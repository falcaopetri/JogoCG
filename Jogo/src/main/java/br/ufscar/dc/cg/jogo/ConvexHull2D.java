package br.ufscar.dc.cg.jogo;

import java.util.ArrayList;
import java.util.List;

public class ConvexHull2D {
    // Source: http://www.java-gaming.org/index.php?topic=522.0

    // Points is filled with points to test, then stripped down to minimal set when hull calcualted
    public static List<Point> calculateHull(List<Point> points) {
        if (points == null || points.size() == 0) {
            return points;
        }

        // Holds the points of the calculated hull
        ArrayList hullPoints = new ArrayList();

        // First find an extreme point guranteed to be on the hull
        // Start from the first point and compare all others for minimal y coord
        Point startPoint = points.get(0);

        for (int i = 0; i < points.size(); i++) {
            Point testPoint = (Point) points.get(i);

            // Find lowest y, and lowest x if equal y values.
            if (testPoint.getY() < startPoint.getY()) {
                startPoint = testPoint;
            } else if (testPoint.getY() == startPoint.getY()) {
                if (testPoint.getX() < startPoint.getX()) {
                    startPoint = testPoint;
                }
            }
        }

        // Add the start point
        hullPoints.add(startPoint);

        Point currentPoint = startPoint;
        Point currentDirection = new Point(1.0f, 0.0f);
        Point nextPoint = null;

        int debug = 0;

        // March around the edge. Finish when we get back to where we started
        while (true) {
            // Find next point with largest right turn relative to current
            double currentAngle = 181f;
            for (int i = 0; i < points.size(); i++) {
                Point testPoint = (Point) points.get(i);

                // Find angle between test and current points
                Point testDirection = new Point(testPoint);
                testDirection.subtract(currentPoint);
                testDirection.normalise();

                double testAngle = currentDirection.angle(testDirection);

                // Update next point with test if smaller angle
                if (testAngle < currentAngle) {
                    currentAngle = testAngle;
                    nextPoint = testPoint;
                } else if (testAngle == currentAngle) {
                    // take point furthest away from current
                    if (currentPoint.distanceTo(testPoint) > currentPoint.distanceTo(nextPoint)) {
                        nextPoint = testPoint;
                    }
                }
            }

            // Exit?
            if (nextPoint == hullPoints.get(0) || debug > 1000) {
                break;
            }

            // Add and advance
            hullPoints.add(nextPoint);

            currentDirection.set(nextPoint);
            currentDirection.subtract(currentPoint);

            currentPoint = nextPoint;
            nextPoint = null;

            debug++;
        }

        return hullPoints;
    }
}
