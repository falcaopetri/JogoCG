package br.ufscar.dc.cg.jogo;

class RGBColor {

    double R, G, B;

    RGBColor(double r, double g, double b) {
        R = r;
        G = g;
        B = b;
    }

    RGBColor(RGBColor color) {
        R = color.R;
        G = color.G;
        B = color.B;
    }
}
