package br.ufscar.dc.cg.jogo.audio;

import static br.ufscar.dc.cg.jogo.audio.IOUtil.ioResourceToByteBuffer;
import static br.ufscar.dc.cg.jogo.audio.OpenALInfo.checkALError;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import org.lwjgl.BufferUtils;
import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.stb.STBVorbis.*;
import org.lwjgl.stb.STBVorbisInfo;
import static org.lwjgl.system.MemoryUtil.NULL;

public class AudioTrack {

    int buffer;
    int source;

    public AudioTrack(String path) {
        buffer = alGenBuffers();
        checkALError();

        source = alGenSources();
        checkALError();

        try (STBVorbisInfo info = STBVorbisInfo.malloc()) {
            ShortBuffer pcm = readVorbis(path, 32 * 1024, info);

            //copy to buffer
            alBufferData(buffer, info.channels() == 1 ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16, pcm, info.sample_rate());
            checkALError();
        }

        //set up source input
        alSourcei(source, AL_BUFFER, buffer);
        checkALError();

        //lets loop the sound
        //alSourcei(source, AL_LOOPING, AL_TRUE);
        //checkALError();
    }

    public void close() {
        //delete buffers and sources
        alDeleteSources(source);
        checkALError();

        alDeleteBuffers(buffer);
        checkALError();
    }

    static ShortBuffer readVorbis(String resource, int bufferSize, STBVorbisInfo info) {
        ByteBuffer vorbis;
        try {
            vorbis = ioResourceToByteBuffer(resource, bufferSize);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        IntBuffer error = BufferUtils.createIntBuffer(1);
        long decoder = stb_vorbis_open_memory(vorbis, error, null);
        if (decoder == NULL) {
            throw new RuntimeException("Failed to open Ogg Vorbis file. Error: " + error.get(0));
        }

        stb_vorbis_get_info(decoder, info);

        int channels = info.channels();

        int lengthSamples = stb_vorbis_stream_length_in_samples(decoder);

        ShortBuffer pcm = BufferUtils.createShortBuffer(lengthSamples);

        pcm.limit(stb_vorbis_get_samples_short_interleaved(decoder, channels, pcm) * channels);
        stb_vorbis_close(decoder);

        return pcm;
    }

    public void play() {
        alSourcePlay(source);
        checkALError();
    }

}
