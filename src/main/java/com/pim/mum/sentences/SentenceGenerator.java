package com.pim.mum.sentences;

import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pim.mum.sentences.WordRepository.WordType;

@Component
public class SentenceGenerator {

	private final Random random = new Random();

	@Autowired
	private WordRepository repository;

	public String generateSentence() {
		final StringBuilder sentence = new StringBuilder();
		for (final WordType type : WordRepository.WordType.values()) {
			// Choose a word:
			final List<String> list = repository.getWords(type);
			final int index = random.nextInt(list.size());
			final String randomWord = list.get(index);
			// Append it to the sentence
			sentence.append(randomWord).append(' ');
		}
		return sentence.toString().trim();
	}
}
