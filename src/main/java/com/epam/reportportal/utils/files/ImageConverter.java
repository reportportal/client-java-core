/*
 * Copyright 2016 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/epam/ReportPortal
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.reportportal.utils.files;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.epam.reportportal.exception.InternalReportPortalClientException;
import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteSource;

/**
 * This class contains functionality for converting images to Black and white
 * colors
 * 
 * @author Aliaksei_Makayed
 * 
 */
public class ImageConverter {

	private static final Logger logger = LoggerFactory.getLogger(ImageConverter.class);

	public static final String IMAGE_TYPE = "image";

	public static ByteSource convertIfImage(ByteSource content) {
		try {
			byte[] data = content.read();
			if (isImage(data)) {
				return convert(data);
			} else {
				return ByteSource.wrap(data);
			}
		} catch (IOException e) {
			throw new InternalReportPortalClientException("Unable to read screenshot file. " + e);
		}
	}

	/**
	 * Convert image to black and white colors
	 * 
	 * @param source
	 * @throws IOException
	 * @throws Exception
	 */
	public static ByteSource convert(byte[] source) throws IOException {
		BufferedImage image;
		image = ImageIO.read(ByteSource.wrap(source).openBufferedStream());
		final BufferedImage blackAndWhiteImage = new BufferedImage(image.getWidth(null), image.getHeight(null),
				BufferedImage.TYPE_BYTE_GRAY);
		final Graphics2D graphics2D = (Graphics2D) blackAndWhiteImage.getGraphics();
		graphics2D.drawImage(image, 0, 0, null);
		graphics2D.dispose();
		return convertToInputStream(blackAndWhiteImage);
	}

	/**
	 * Check is input file is image
	 * 
	 * @param fileContent
	 */
	public static boolean isImage(byte[] fileContent) {

		AutoDetectParser parser = new AutoDetectParser();
		Detector detector = parser.getDetector();
		MediaType mediaType;
		try {
			mediaType = detector.detect(TikaInputStream.get(fileContent), new Metadata());
		} catch (Exception e) {
			logger.error("Unable to read file content.", e);
			throw new InternalReportPortalClientException("Unable to read file content.", e);
		}
		return mediaType.toString().contains(IMAGE_TYPE);
	}

	/**
	 * Convert BufferedImage to input stream
	 * 
	 * @param image
	 * @throws IOException
	 */
	private static ByteSource convertToInputStream(BufferedImage image) {
		ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
		try {
			ImageIO.write(image, "png", byteOutputStream);
		} catch (IOException e) {
			throw new InternalReportPortalClientException("Unable to transform file to byte array.", e);
		}
		return ByteSource.wrap(byteOutputStream.toByteArray());

	}

}
