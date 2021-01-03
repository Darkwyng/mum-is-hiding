package com.pim.mum.sentences;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class WordRepository {

	private static final Logger logger = LoggerFactory.getLogger(WordRepository.class);

	@Autowired
	private MumIsHidingProperties properties;

	private Map<WordType, List<String>> words = null;

	/** Called at startup to fill the repository from the available files. */
	@PostConstruct
	public void initialize() throws URISyntaxException {
		final Map<WordType, List<String>> collectedWords = readFromDataDirectory();
		Assert.isTrue(collectedWords.size() == WordType.values().length,
				"Should have filled map completely: " + collectedWords.size());
		addMissingWordsFromClassPath(collectedWords);
		validateAndLog(collectedWords);
	}

	/** Called when a sentence should be generated. */
	public List<String> getWords(final WordType type) {
		return words.get(type);
	}

	/** Called when one of the files has changed. */
	public void handleChangedFile(final File file) {
		final Optional<WordType> optional = Arrays.stream(WordType.values())
				.filter(type -> type.getFileName().equals(file.getName())).findAny();
		if (optional.isEmpty()) {
			logger.info("Ignoring {}, because it does not match any of the monitored files.", file.getAbsolutePath());
		} else {
			final WordType type = optional.get();
			final List<String> newWords = readSingleType(Path.of(properties.getDataPath(), type.getFileName()), type);
			if (newWords.isEmpty()) {
				logger.info("Ignoring {}, because it is empty.", file.getAbsolutePath());
			} else {
				words.put(type, newWords);
				logger.info("Read {} words from {}", newWords.size(), file.getAbsolutePath());
			}
		}
	}

	private void addMissingWordsFromClassPath(final Map<WordType, List<String>> collectedWords)
			throws URISyntaxException {
		for (final Entry<WordType, List<String>> entry : collectedWords.entrySet()) {
			if (entry.getValue().isEmpty()) {
				final WordType type = entry.getKey();
				logger.debug("{} has no words yet.", type);
				final URL url = WordRepository.class.getResource(type.getFileName());
				final Path res = Path.of(url.toURI());
				final List<String> list = readSingleType(res, type);
				collectedWords.put(type, list);
			}
		}
	}

	private void validateAndLog(final Map<WordType, List<String>> collectedWords) {
		boolean allOkay = true;
		final StringBuilder message = new StringBuilder();
		for (final WordType type : WordType.values()) {
			final int size = collectedWords.get(type).size();

			message.append(type).append(" has ").append(size).append(" entries. ");

			if (size == 0) {
				allOkay = false;
				message.append("That's NOT okay. ");
			}
		}

		if (allOkay) {
			logger.info(message.toString());
			words = collectedWords;
		} else {
			throw new IllegalStateException(message.toString());
		}
	}

	private Map<WordType, List<String>> readFromDataDirectory() {
		final String directory = properties.getDataPath();
		logger.info("Reading from directory {}", directory);

		if (!Path.of(directory).toFile().exists()) {
			logger.error("The data directory does not exist: {}", directory);
		}

		final Map<WordType, List<String>> result = new HashMap<>();
		for (final WordType type : WordType.values()) {
			final Path path = Path.of(directory, type.getFileName());
			final List<String> list = readSingleType(path, type);
			result.put(type, list);
		}

		return result;
	}

	private List<String> readSingleType(final Path path, final WordType type) {
		if (path.toFile().exists()) {
			logger.info("Reading {} from {}", type, path);
			try {
				return Files.lines(path).map(word -> word.trim()).collect(Collectors.toCollection(ArrayList::new));
			} catch (final IOException e) {
				logger.warn("Reading {} from {} failed due to ''{}''. Defaults will be used.", type, path,
						e.getMessage());
				logger.debug(e.getMessage(), e);
				return new ArrayList<>();
			}
		} else {
			logger.info("{} cannot be read from {}, because the file does not exist. Defaults will be used.", type,
					path);
			return new ArrayList<>();
		}
	}

	public static enum WordType {

		SUBJECT, PREDICATE, ADJECTIVE, OBJECT, LOCATION;

		private String getFileName() {
			return name().toLowerCase(Locale.ENGLISH) + "s.txt";
		}
	}

}
