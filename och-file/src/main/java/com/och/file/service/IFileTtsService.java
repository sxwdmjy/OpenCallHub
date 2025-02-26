package com.och.file.service;

import java.io.File;
import java.util.function.Consumer;

public interface IFileTtsService {

    void textToSpeech(String text, Integer type, Consumer<File> fileConsumer);
}
