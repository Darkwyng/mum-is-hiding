package com.pim.mum.sentences;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Set;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.devtools.filewatch.ChangedFile;
import org.springframework.boot.devtools.filewatch.ChangedFiles;
import org.springframework.boot.devtools.filewatch.FileChangeListener;
import org.springframework.boot.devtools.filewatch.FileSystemWatcher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.pim.mum.sentences.WordRepository;

@Configuration
public class FileChangeWatcher implements FileChangeListener {

	private static final Logger logger = LoggerFactory.getLogger(FileChangeWatcher.class);

	@Autowired
	private MumIsHidingProperties properties;
	@Autowired
	private WordRepository repository;

	@Bean
	public FileSystemWatcher fileSystemWatcher() {
		final FileSystemWatcher fileSystemWatcher = new FileSystemWatcher(true, Duration.ofSeconds(30),
				Duration.ofSeconds(5));
		fileSystemWatcher.addSourceDirectory(Path.of(properties.getDataPath()).toFile());
		fileSystemWatcher.addListener(this);
		fileSystemWatcher.start();

		logger.info("Started");
		return fileSystemWatcher;
	}

	@PreDestroy
	public void onDestroy() throws Exception {
		logger.info("Stopping");
		fileSystemWatcher().stop();
	}

	@Override
	public void onChange(final Set<ChangedFiles> changeSet) {
		logger.info("Change to file registered");
		for (final ChangedFiles files : changeSet) {
			for (final ChangedFile file : files.getFiles()) {
				logger.debug("{}", file.getFile());
				repository.handleChangedFile(file.getFile());
			}
		}
	}
}
