package matkuldesktop;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;

import javax.swing.*;
import java.awt.Dimension;
import java.awt.image.BufferedImage;


public class barcodeScanner {

    public static void mulaiScan() {
        Webcam webcam = Webcam.getDefault();
        webcam.setViewSize(new Dimension(640, 480));

        WebcamPanel panel = new WebcamPanel(webcam);
        panel.setMirrored(true);

        JFrame window = new JFrame("Scan Barcode");
        window.add(panel);
        window.pack();
        window.setVisible(true);

            webcam.close();
    }
}
