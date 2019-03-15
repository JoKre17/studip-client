package de.kriegel.studip.client.content.model.file;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import de.kriegel.studip.client.content.model.data.FileRef;
import de.kriegel.studip.client.content.model.data.Folder;

public class FileRefNode implements Serializable {

	private FileRef fileRef;
	private Folder folder;
	private boolean isDirectory = false;

	private List<FileRefNode> children;

	public FileRefNode(FileRef fileRef) {
		this.fileRef = fileRef;
	}

	public FileRefNode(Folder folder) {
		this.folder = folder;
		this.children = new ArrayList<>();
		isDirectory = true;
	}

	public FileRef getFileRef() {
		return fileRef;
	}

	public Folder getFolder() {
		return folder;
	}

	public List<FileRefNode> getChildren() {
		return children;
	}

	public boolean isDirectory() {
		return isDirectory;
	}

	public void addFileRefNode(FileRefNode fileNode) throws Exception {
		if (isDirectory) {
			this.children.add(fileNode);
		} else {
			throw new Exception("This FileNode is no Directory!");
		}
	}

	public int getSize() {
		int sum = 1;

		if (isDirectory) {
			for (FileRefNode node : children) {
				sum += node.getSize();
			}
		}
		
		return sum;
	}

}