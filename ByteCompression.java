package compression;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author B Ricks, PhD <bricks@unomaha.edu>
 */
public class Compression {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {

            BufferedImage original = ImageIO.read(new File("Bridge.jpeg"));

            String compressedFilename = compress(original);

            BufferedImage decompressed = decompress(compressedFilename);

            ImageIO.write(decompressed, "PNG", new File("Bridge.png"));
            
        } catch (IOException ex) {
            Logger.getLogger(Compression.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static String compress(BufferedImage original) {

        String filename = "compressed.txt";

        try (BufferedOutputStream br = new BufferedOutputStream(new FileOutputStream(filename))) {
            int width = original.getWidth();
            int height = original.getHeight();
            br.write(("" + width).getBytes());
            br.write((" ").getBytes());
            br.write(("" + height).getBytes());
            br.write("\r\n".getBytes());

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    Color pixel = new Color(original.getRGB(x, y));

                    int r = pixel.getRed();
                    int g = pixel.getGreen();
                    int b = pixel.getBlue();

                    byte rByte = (byte) r;
                    byte gByte = (byte) g;
                    byte bByte = (byte) b;

                    br.write(r);
                    br.write(g);
                    br.write(b);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Compression.class.getName()).log(Level.SEVERE, null, ex);
        }

        return filename;

    }

    private static BufferedImage decompress(String compressed) {

        BufferedImage bi = null;

        try (FileInputStream fr = new FileInputStream(compressed)) {

            int endOfLineIndex = 0;
            byte fileContent[] = new byte[(int) new File(compressed).length()];
            fr.read(fileContent);
            while (endOfLineIndex < fileContent.length && fileContent[endOfLineIndex++] != 0x0A);

            if (endOfLineIndex >= fileContent.length) {
                throw new RuntimeException("Couldn't find the first line of the file");
            }

            byte[] header = Arrays.copyOfRange(fileContent, 0, endOfLineIndex);
            String headerString = new String(header);
            String[] widthAndHeight = headerString.split(" ");
            int width = Integer.parseInt(widthAndHeight[0].trim());
            int height = Integer.parseInt(widthAndHeight[1].trim());
            bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            int index = endOfLineIndex;

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int r = Byte.toUnsignedInt(fileContent[index]);
                    int g = Byte.toUnsignedInt(fileContent[index + 1]);
                    int b = Byte.toUnsignedInt(fileContent[index + 2]);

                    Color pixel = new Color(r, g, b);

                    bi.setRGB(x, y, pixel.getRGB());

                    index += 3;
                }
            }
        } catch (IOException ex) {}

        return bi;
    }
}
