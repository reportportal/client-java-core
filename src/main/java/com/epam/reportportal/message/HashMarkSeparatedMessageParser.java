/*
 * Copyright 2016 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/client-java-core
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
package com.epam.reportportal.message;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.io.BaseEncoding;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import com.google.common.io.Resources;

import java.io.File;
import java.net.URL;
import java.util.List;

/**
 * Colon separated message parser. Expects string in the following format:<br>
 * RP_MESSAGE#FILE#FILENAME#MESSAGE_TEST<br>
 * RP_MESSAGE#BASE64#BASE_64_REPRESENTATION#MESSAGE_TEST<br>
 *
 * @author Andrei Varabyeu
 */
public class HashMarkSeparatedMessageParser implements MessageParser {

	/**
	 * Different representations of binary data
	 */
	private enum MessageType {
		FILE {
			@Override
			public ByteSource toByteSource(String data) {
				File file = new File(data);
				return file.exists() ? Files.asByteSource(file) : null;
			}
		},
		BASE64 {
			@Override
			public ByteSource toByteSource(final String data) {
				return ByteSource.wrap(BaseEncoding.base64().decode(data));
			}
		},
		RESOURCE {
			@Override
			public ByteSource toByteSource(String data) {
				URL resource = Resources.getResource(data);
				return null == resource ? null : Resources.asByteSource(resource);
			}
		};

		abstract public ByteSource toByteSource(String data);

		public static MessageType fromString(String messageType) {
			return MessageType.valueOf(messageType);
		}
	}

	private static final int CHUNKS_COUNT = 4;

	@Override
	public ReportPortalMessage parse(String message) {
		Iterable<String> splitted = Splitter.on("#").limit(CHUNKS_COUNT).split(message);

		List<String> splittedAsList = ImmutableList.<String>builder().addAll(splitted).build();

		// -1 because there may be no
		if (CHUNKS_COUNT != splittedAsList.size()) {
			throw new RuntimeException(
					"Incorrect message format. Chunks: " + Joiner.on("\n").join(splittedAsList) + "\n count: " + splittedAsList.size());
		}
		return new ReportPortalMessage(MessageType.fromString(splittedAsList.get(1)).toByteSource(splittedAsList.get(2)),
				splittedAsList.get(3));
	}

	@Override
	public boolean supports(String message) {
		return message.startsWith(RP_MESSAGE_PREFIX);
	}
}
