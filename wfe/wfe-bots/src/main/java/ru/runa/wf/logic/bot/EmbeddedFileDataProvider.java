package ru.runa.wf.logic.bot;

import ru.runa.wfe.definition.BotTaskFileDoesNotExistException;
import ru.runa.wfe.service.client.DelegateFileDataProvider;
import ru.runa.wfe.user.User;

public class EmbeddedFileDataProvider extends DelegateFileDataProvider {
	byte[] embeddedFile;
	
	public EmbeddedFileDataProvider(User user, Long definitionId) {
		super(user, definitionId);
	}
	
	public EmbeddedFileDataProvider(User user, Long definitionId, byte[] embeddedFile) {
		super(user, definitionId);
		this.embeddedFile = embeddedFile;
	}

	@Override
	public byte[] getFileData(String fileName) {
		if (fileName.startsWith(BOT_TASK_FILE_PROTOCOL)) {
			return embeddedFile;
		}
		String realFileName = fileName;
		if (fileName.startsWith(PROCESS_FILE_PROTOCOL)) {
			realFileName = fileName.substring(PROCESS_FILE_PROTOCOL.length());
		}
		return super.getFileData(realFileName);
	}

	@Override
	public byte[] getFileDataNotNull(String fileName) {
		if(fileName.startsWith(BOT_TASK_FILE_PROTOCOL)) {
			byte[] data = getFileData(fileName);
	        if (data == null) {
	            throw new BotTaskFileDoesNotExistException(fileName);
	        }
			return embeddedFile;
		} else {
			return super.getFileDataNotNull(fileName);
		}
	}
}
