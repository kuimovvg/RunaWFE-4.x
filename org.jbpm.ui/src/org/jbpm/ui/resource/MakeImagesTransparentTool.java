package org.jbpm.ui.resource;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class MakeImagesTransparentTool {

    public static void main(String[] args) throws Exception {
    }

    public static void makeImageTransparent(String imagePath) throws FileNotFoundException {
        ImageLoader imageLoader = new ImageLoader();
        imageLoader.load(new FileInputStream(imagePath));
        int whitePixel = imageLoader.data[0].palette.getPixel(new RGB(255,255,255));
        for (int i = 0; i < imageLoader.data.length; i++) {
            imageLoader.data[i].transparentPixel = whitePixel;
        }
        imageLoader.save(imagePath + ".tr.gif", SWT.IMAGE_GIF);
    }

    public static void paintAndSave(IFigure figure, String outputPath) {
        Image image = new Image(Display.getDefault(), 17, 17);
        SWTGraphics g = new SWTGraphics(new GC(image));
        g.setAntialias(SWT.ON);
        figure.paint(g);
        ImageLoader imageLoader = new ImageLoader();
        imageLoader.data = new ImageData[] { image.getImageData() };
        int whitePixel = imageLoader.data[0].palette.getPixel(new RGB(255,255,255));
        for (int i = 0; i < imageLoader.data.length; i++) {
            imageLoader.data[i].transparentPixel = whitePixel;
        }
        imageLoader.save(outputPath, SWT.IMAGE_PNG);
    }
}
