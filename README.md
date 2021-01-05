# ImageProcessingTool

## About
**ImageProcessingTool** is a command-line tool for image conversion and processing.

Supported image file formats: png, jpeg, gif, bmp, wbmp
 
Pixel data support table:

 | File format | RGB | ARGB | ARGB_PRE | BGR | BGR_3BYTE | ABGR_4BYTE | ABGR_4BYTE_PRE | RGB_565 | GRAY_BYTE | GRAY_USHORT | BINARY_BYTE |
 |:-----------:|:---:|:----:|:--------:|:---:|:---------:|:----------:|:--------------:|--------:|----------:|------------:|------------:|
 | **png**     | yes | yes  | yes      | yes | yes       | yes        | yes            | yes     | yes       | yes         | yes         |
 | **jpeg**    | yes | no   | no       | yes | yes       | no         | no             | yes     | yes       | yes         | yes         |
 | **gif**     | yes | yes  | yes      | yes | yes       | yes        | yes            | yes     | yes       | yes         | yes         |
 | **bmp**     | yes | no   | no       | yes | yes       | no         | no             | yes     | yes       | yes         | yes         |
 | **wbmp**    | no  | no   | no       | no  | no        | no         | no             | no      | no        | no          | yes         |

## ImageProcessingTool options

##### Print tool version to stdout and exit
```bash
-version
```

##### Common
```bash
-inputPath "path/to/input/image.png"
-outputPath "path/to/output/image.bmp"
-overwrite #Must be set if you want to override existing files
-saveAs GRAY_BYTE #Optional. Set if you want to change how pixel data are stored.
-colorizeTransparentPixels FF0000 #Optional. Change color of all fully transparent pixels.
-blendColorAroundVisiblePixels #Set if you want to blend color around visible pixels. 
-removeTransparency #Set if you want to remove transparency from pixels.
```

## Releases
Latest release: [1.0.0](https://github.com/chorobochrontochor/ImageProcessingTool/releases)
