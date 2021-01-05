package chchch;

import java.awt.image.BufferedImage;

class ImageInfo
{
    int fullyTransparentPixels;
    int partiallyTransparentPixels;
    int opaquePixels;
    int nonTransparentColors;
    int dominantNonTransparentColor;

    ImageInfo(int fullyTransparentPixels, int partiallyTransparentPixels, int opaquePixels, int nonTransparentColors, int dominantNonTransparentColor)
    {
        this.fullyTransparentPixels = fullyTransparentPixels;
        this.partiallyTransparentPixels = partiallyTransparentPixels;
        this.opaquePixels = opaquePixels;
        this.nonTransparentColors = nonTransparentColors;
        this.dominantNonTransparentColor = dominantNonTransparentColor;
    }
    static void printImageInfo(BufferedImage inputImage, ImageInfo inputImageInfo, BufferedImage outputImage, ImageInfo outputImageInfo)
    {
        System.out.println(" Type: " + inputImage.getType() + " > " + outputImage.getType());
        System.out.println(" Width: " + inputImage.getWidth() + " > " + outputImage.getWidth());
        System.out.println(" Height: " + inputImage.getHeight() + " > " + outputImage.getHeight());
        System.out.println(" NumComponents: " + inputImage.getColorModel().getNumComponents() + " > " + outputImage.getColorModel().getNumComponents());
        System.out.println(" PixelSize: " + inputImage.getColorModel().getPixelSize() + " > " + outputImage.getColorModel().getPixelSize());
        System.out.println(" Transparency: " + inputImage.getTransparency() + " > " + outputImage.getTransparency());
        System.out.println(" HasAlpha: " + inputImage.getColorModel().hasAlpha() + " > " + outputImage.getColorModel().hasAlpha());
        System.out.println(" Alpha is premultiplied: " + inputImage.isAlphaPremultiplied() + " > " + outputImage.isAlphaPremultiplied());
        System.out.println(" Fully transparent pixel count: " + inputImageInfo.fullyTransparentPixels + " > " + outputImageInfo.fullyTransparentPixels);
        System.out.println(" Partially Transparent pixel count: " + inputImageInfo.partiallyTransparentPixels + " > " + outputImageInfo.partiallyTransparentPixels);
        System.out.println(" Opaque pixel count: " + inputImageInfo.opaquePixels + " > " + outputImageInfo.opaquePixels);
        System.out.println(" Non-transparent color count: " + inputImageInfo.nonTransparentColors + " > " + outputImageInfo.nonTransparentColors);
    }
}