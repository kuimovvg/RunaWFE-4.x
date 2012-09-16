/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */

package ru.runa.notifier.util;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.graphics.Image;

import ru.runa.notifier.GUI;

/**
 * Created on 2006
 * 
 * @author Gritsenko_S
 */
public class ImageManager {
	private static final Log log = LogFactory.getLog(ImageManager.class);

	public static Image iconTrayNoTasks;

	public static Image iconTrayTasks;

	public static Image iconTrayNotLogged;

	public static Image iconTrayError;

	public static Image iconApplication;

	public static Image iconTrayClose;

	public static Image iconTrayCloseOver;

	public static Image iconTrayTease;

	public static Image imageSplash;

	private static final Class clazz = ImageManager.class;

	public static void disposeIcons() {
		iconTrayTasks.dispose();
		iconApplication.dispose();
		iconTrayClose.dispose();
		iconTrayCloseOver.dispose();
		iconTrayTease.dispose();
		iconTrayNoTasks.dispose();
		iconTrayNotLogged.dispose();
		imageSplash.dispose();
	}

	public static void initIcons() {
		iconTrayTasks = loadImage("/img/tasks.gif");
		iconApplication = loadImage("/img/application.gif");
		iconTrayClose = loadImage("/img/tray_close.gif");
		iconTrayCloseOver = loadImage("/img/tray_close_over.gif");
		iconTrayTease = loadImage("/img/tray_tease.gif");
		iconTrayNoTasks = loadImage("/img/tray.gif");
		iconTrayNotLogged = loadImage("/img/tray_not_logged.gif");
		iconTrayError = loadImage("/img/tray_error.gif");
		imageSplash = loadImage("/img/splash.gif");
	}

	private static Image loadImage(String path) {
		Image image;
		InputStream inS = null;
		try {
			inS = clazz.getResourceAsStream(path);
			if (inS != null) {
				image = new Image(GUI.display, inS);
			} else {
				image = new Image(GUI.display, 16, 16);
			}
		} finally {
			if (inS != null) {
				try {
					inS.close();
				} catch (IOException e) {
					if (log.isDebugEnabled()) {
						log.debug("Error closing image " + path, e);
					}
				}
			}
		}

		return image;
	}
}
