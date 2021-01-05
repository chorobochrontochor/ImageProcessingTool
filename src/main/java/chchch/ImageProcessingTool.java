package chchch;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ImageProcessingTool
{
    private static boolean isFullyTransparent(Color color)
    {
        return color.getAlpha() == 0;
    }
    private static Color lerp(Color a, Color b, float t)
    {
        if (a != null && b != null)
        {
            float it = 1f - t;
            int redPart = (int) (a.getRed() * t + b.getRed() * it);
            int greenPart = (int) (a.getGreen() * t + b.getGreen() * it);
            int bluePart = (int) (a.getBlue() * t + b.getBlue() * it);
            return new Color(redPart, greenPart, bluePart);
        }
        return a == null ? b : a;
    }
    private static Color lerp4(Color a, Color b, Color aa, Color bb)
    {
        Color vertical = lerp(a, aa, 0.5f);
        Color horizontal = lerp(b, bb, 0.5f);
        return lerp(vertical, horizontal, 0.5f);
    }
    private static Color getColorIfVisible(BufferedImage source, int x, int y)
    {
        if (x < 0 || x >= source.getWidth()) return null;
        if (y < 0 || y >= source.getHeight()) return null;
        Color color = new Color(source.getRGB(x, y), true);
        if (isFullyTransparent(color)) return null;
        return color;
    }
    private static Color GetSurroundingNonTransparentInterpolation(BufferedImage source, int x, int y, int transparentColor)
    {
        Color up = getColorIfVisible(source, x, y - 1);
        Color right = getColorIfVisible(source, x + 1, y);
        Color down = getColorIfVisible(source, x, y + 1);
        Color left = getColorIfVisible(source, x - 1, y);

        Color upRight = getColorIfVisible(source, x + 1, y - 1);
        Color downRight = getColorIfVisible(source, x + 1, y + 1);
        Color downLeft = getColorIfVisible(source, x - 1, y + 1);
        Color upLeft = getColorIfVisible(source, x - 1, y - 1);

        Color straight = lerp4(up, right, down, left);
        Color diagonal = lerp4(upRight, downRight, downLeft, upLeft);

        Color finalInterpolation = lerp(straight, diagonal, 0.35f); // straight are "closer"
        if (finalInterpolation != null)
        {
            return new Color(finalInterpolation.getRed(), finalInterpolation.getGreen(), finalInterpolation.getBlue(), 0);
        }
        return new Color(transparentColor, true);
    }
    private static BufferedImage ColorizeTransparentPixels(BufferedImage source, int transparentColor)
    {
        transparentColor &= 0xFFFFFF;
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
        for (int y = 0; y < source.getHeight(); y++)
        {
            for (int x = 0; x < source.getWidth(); x++)
            {
                Color color = new Color(source.getRGB(x, y), true);
                if (color.getAlpha() == 0)
                {
                    color = new Color(transparentColor, true);
                }
                result.setRGB(x, y, color.getRGB());
            }
        }
        return result;
    }
    private static BufferedImage RemoveTransparency(BufferedImage source)
    {
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
        for (int y = 0; y < source.getHeight(); y++)
        {
            for (int x = 0; x < source.getWidth(); x++)
            {
                Color color = new Color(source.getRGB(x, y), true);
                result.setRGB(x, y, color.getRGB() | 0xFF000000);
            }
        }
        return result;
    }
    private static BufferedImage BlendColorAroundVisiblePixels(BufferedImage source, int transparentColor)
    {
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
        for (int y = 0; y < source.getHeight(); y++)
        {
            for (int x = 0; x < source.getWidth(); x++)
            {
                Color color = new Color(source.getRGB(x, y), true);
                if (color.getAlpha() == 0)
                {
                    color = GetSurroundingNonTransparentInterpolation(source, x, y, transparentColor);
                }
                result.setRGB(x, y, color.getRGB());
            }
        }
        return result;
    }
    private static int getDominantColor(Map<Integer, Integer> colorMap)
    {
        int result = 0;
        int colorCount = 0;
        for (Integer colorKey : colorMap.keySet()) {
            if (colorMap.get(colorKey) > colorCount) {
                colorCount = colorMap.get(colorKey);
                result = colorKey;
            }
        }
        return result;
    }
    private static ImageInfo processImageInfo(BufferedImage bufferedImage)
    {
        Map<Integer, Integer> nonTransparentColorMap = new HashMap<>();
        int fullyTransparentPixels = 0;
        int partiallyTransparentPixels = 0;
        int opaquePixels = 0;
        for (int y = 0; y < bufferedImage.getHeight(); y++)
        {
            for (int x = 0; x < bufferedImage.getWidth(); x++)
            {
                Color color = new Color(bufferedImage.getRGB(x, y), bufferedImage.getTransparency() != Transparency.OPAQUE);
                Integer colorWithoutAlpha = color.getRGB() & 0xFFFFFF;
                if (color.getAlpha() == 0) {
                    fullyTransparentPixels++;
                } else if (color.getAlpha() == 255) {
                    opaquePixels++;
                    nonTransparentColorMap.put(colorWithoutAlpha, nonTransparentColorMap.getOrDefault(colorWithoutAlpha, 0) + 1);
                } else {
                    partiallyTransparentPixels++;
                }
            }
        }
        int dominantNonTransparentColor = getDominantColor(nonTransparentColorMap);
        return new ImageInfo(fullyTransparentPixels, partiallyTransparentPixels, opaquePixels, nonTransparentColorMap.size(), dominantNonTransparentColor);
    }
    public static void main(String[] args)
    {
        Map<String, Integer> saveAsTypeMap = new HashMap<String, Integer>() {{
            put("RGB", BufferedImage.TYPE_INT_RGB);
            put("ARGB", BufferedImage.TYPE_INT_ARGB);
            put("ARGB_PRE", BufferedImage.TYPE_INT_ARGB_PRE);
            put("BGR", BufferedImage.TYPE_INT_BGR);
            put("BGR_3BYTE", BufferedImage.TYPE_3BYTE_BGR);
            put("ABGR_4BYTE", BufferedImage.TYPE_4BYTE_ABGR);
            put("ABGR_4BYTE_PRE", BufferedImage.TYPE_4BYTE_ABGR_PRE);
            put("RGB_565", BufferedImage.TYPE_USHORT_565_RGB);
            put("GRAY_BYTE", BufferedImage.TYPE_BYTE_GRAY);
            put("GRAY_USHORT", BufferedImage.TYPE_USHORT_GRAY);
            put("BINARY_BYTE", BufferedImage.TYPE_BYTE_BINARY);
        }};
        Map<String, String> extensionFormatMap = new HashMap<String, String>() {{
            put("jpg", "jpeg");
            put("bmp", "bmp");
            put("gif", "gif");
            put("png", "png");
            put("jpeg", "jpg");
            put("wbmp", "wbmp");
        }};
        Map<String, Integer[]> formatAvailableTypeMap = new HashMap<String, Integer[]>() {{
            put("jpg", new Integer[]{BufferedImage.TYPE_INT_RGB, BufferedImage.TYPE_INT_BGR, BufferedImage.TYPE_3BYTE_BGR, BufferedImage.TYPE_USHORT_565_RGB, BufferedImage.TYPE_BYTE_GRAY, BufferedImage.TYPE_USHORT_GRAY, BufferedImage.TYPE_BYTE_BINARY, BufferedImage.TYPE_BYTE_INDEXED});
            put("jpeg", new Integer[]{BufferedImage.TYPE_INT_RGB, BufferedImage.TYPE_INT_BGR, BufferedImage.TYPE_3BYTE_BGR, BufferedImage.TYPE_USHORT_565_RGB, BufferedImage.TYPE_BYTE_GRAY, BufferedImage.TYPE_USHORT_GRAY, BufferedImage.TYPE_BYTE_BINARY, BufferedImage.TYPE_BYTE_INDEXED});
            put("bmp", new Integer[]{BufferedImage.TYPE_INT_RGB, BufferedImage.TYPE_INT_BGR, BufferedImage.TYPE_3BYTE_BGR, BufferedImage.TYPE_USHORT_565_RGB, BufferedImage.TYPE_BYTE_GRAY, BufferedImage.TYPE_USHORT_GRAY, BufferedImage.TYPE_BYTE_BINARY, BufferedImage.TYPE_BYTE_INDEXED});
            put("gif", new Integer[]{BufferedImage.TYPE_INT_RGB, BufferedImage.TYPE_INT_ARGB, BufferedImage.TYPE_INT_ARGB_PRE, BufferedImage.TYPE_INT_BGR, BufferedImage.TYPE_3BYTE_BGR, BufferedImage.TYPE_4BYTE_ABGR, BufferedImage.TYPE_4BYTE_ABGR_PRE, BufferedImage.TYPE_USHORT_565_RGB, BufferedImage.TYPE_USHORT_555_RGB, BufferedImage.TYPE_BYTE_GRAY, BufferedImage.TYPE_USHORT_GRAY, BufferedImage.TYPE_BYTE_BINARY, BufferedImage.TYPE_BYTE_INDEXED});
            put("png", new Integer[]{BufferedImage.TYPE_INT_RGB, BufferedImage.TYPE_INT_ARGB, BufferedImage.TYPE_INT_ARGB_PRE, BufferedImage.TYPE_INT_BGR, BufferedImage.TYPE_3BYTE_BGR, BufferedImage.TYPE_4BYTE_ABGR, BufferedImage.TYPE_4BYTE_ABGR_PRE, BufferedImage.TYPE_USHORT_565_RGB, BufferedImage.TYPE_USHORT_555_RGB, BufferedImage.TYPE_BYTE_GRAY, BufferedImage.TYPE_USHORT_GRAY, BufferedImage.TYPE_BYTE_BINARY, BufferedImage.TYPE_BYTE_INDEXED});
            put("wbmp", new Integer[]{BufferedImage.TYPE_BYTE_BINARY});
        }};

        try
        {
            System.out.println(Base.getFullName(ImageProcessingTool.class));
            if (Base.hasArg(args, "-version")) {
                System.exit(0);
            }

            System.out.print("Processing command line arguments...");
            String inputPath = Base.getArgValue(args, "-inputPath", false, null);
            String outputPath = Base.getArgValue(args, "-outputPath", false, null);
            String saveAs = Base.getArgValue(args, "-saveAs", true, null);
            boolean overwrite = Base.hasArg(args, "-overwrite");
            boolean blendColorAroundVisiblePixels = Base.hasArg(args, "-blendColorAroundVisiblePixels");
            String colorizeTransparentPixels = Base.getArgValue(args, "-colorizeTransparentPixels", false, null);
            boolean removeTransparency = Base.hasArg(args, "-removeTransparency");

            File outputFile = new File(outputPath);
            String outputExtension = Base.getFileExtension(outputFile);
            if (!extensionFormatMap.containsKey(outputExtension)) {
                throw new Exception("Extension \"" + outputExtension + "\" is not supported!");
            }
            System.out.println("Done.");

            System.out.println();
            System.out.println("Settings: ");
            System.out.println(" - inputPath: " + inputPath);
            System.out.println(" - outputPath: " + outputPath);
            System.out.println(" - overrideType: " + saveAs);
            System.out.println(" - overwrite: " + overwrite);
            System.out.println(" - blendColorAroundVisiblePixels: " + blendColorAroundVisiblePixels);
            System.out.println(" - colorizeTransparentPixels: " + colorizeTransparentPixels);
            System.out.println(" - removeTransparency: " + removeTransparency);
            System.out.println();

            BufferedImage inputImage = ImageIO.read(new File(inputPath));
            String outputFormat = extensionFormatMap.get(outputExtension);
            int outputType = inputImage.getType();
            if (saveAs != null) {
                if (!saveAsTypeMap.containsKey(saveAs)) {
                    throw new Exception("Unknown saveAs value \"" + saveAs + "\". Available values: " + String.join(", ", saveAsTypeMap.keySet()));
                }
                outputType = saveAsTypeMap.get(saveAs);
            }

            if (!Base.arrayContains(formatAvailableTypeMap.get(outputFormat), outputType)) {
                throw new Exception("\"" + saveAs + "\" is not supported for \"" + outputFormat + "\"!");
            }

            if (outputFile.exists()) {
                if (!overwrite) {
                    throw new IOException("Destination path already exists!");
                }
                if (outputFile.isDirectory()) {
                    throw new IOException("Destination path is a folder!");
                }
            }

            ImageInfo inputImageInfo = processImageInfo(inputImage);

            if (outputType == 13) {
                throw new Exception("Type \"" + saveAs + "\" is currently not supported!");

//                IndexColorModel cm = new IndexColorModel(
//                    3, // 3 bits can store up to 8 colors
//                    6, // here I use only 6
//                    //          RED  GREEN1 GREEN2  BLUE  WHITE BLACK
//                    new byte[]{-100, 0, 0, 0, -1, 0},
//                    new byte[]{0, -100, 60, 0, -1, 0},
//                    new byte[]{0, 0, 0, -100, -1, 0}
//                );
//                outputImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), destinationType, cm);
            }

            BufferedImage outputImage = new BufferedImage(inputImage.getWidth(), inputImage.getHeight(), outputType);

            outputImage.getGraphics().drawImage(inputImage, 0, 0, null);

            if (blendColorAroundVisiblePixels) {
                int transparentColor = inputImageInfo.dominantNonTransparentColor;

                outputImage = BlendColorAroundVisiblePixels(outputImage, transparentColor & 0xFFFFFF);
            }

            if (colorizeTransparentPixels != null) {
                int transparentColor;
                try
                {
                    transparentColor = Integer.parseInt(colorizeTransparentPixels, 16);
                }
                catch (Exception exception)
                {
                    throw new Exception("Invalid hexadecimal value for -colorizeTransparentPixels!");
                }
                outputImage = ColorizeTransparentPixels(outputImage, transparentColor);
            }

            if (removeTransparency) {
                outputImage = RemoveTransparency(outputImage);
            }

            ImageInfo outputImageInfo = processImageInfo(outputImage);

            System.out.println("Info:");
            ImageInfo.printImageInfo(inputImage, inputImageInfo, outputImage, outputImageInfo);
            System.out.println();

            try {
                if (!ImageIO.write(outputImage, outputFormat, outputFile)) {
                    throw new Exception("Unsupported format!");
                }
            } catch (Exception exception) {
                System.out.println(exception.getMessage());
            }

            System.exit(0);
        }
        catch (Exception exception)
        {
            System.out.println("Failed.");
            System.err.println("Error: " + exception.getMessage());
            System.exit(1);
        }
    }
}