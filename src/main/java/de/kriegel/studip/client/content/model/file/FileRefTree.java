package de.kriegel.studip.client.content.model.file;

import java.io.StringWriter;

import de.kriegel.studip.client.content.model.data.FileRef;
import de.kriegel.studip.client.content.model.data.Folder;

public class FileRefTree {

	private FileRefNode root;

	public FileRefTree(FileRef rootFileRef) {
		root = new FileRefNode(rootFileRef);
	}

	public FileRefTree(Folder rootFolder) {
		root = new FileRefNode(rootFolder);
	}

	public FileRefNode createFileNode(FileRef fileRef) {
		return new FileRefNode(fileRef);
	}

	public FileRefNode getRoot() {
		return root;
	}

	public int getSize() {
		return root.getSize();
	}

	public FileRefNode createFileNode(Folder folder) {
		return new FileRefNode(folder);
	}

	private String traverseTreeInorder(FileRefNode root) {
		StringWriter resultWriter = new StringWriter();

		if (root.isDirectory()) {
			resultWriter.append(root.getFolder().getName() + " " + root.getChildren().size() + " "
					+ root.getFolder().getSubfolders().size() + " " + root.getFolder().getFileRefs().size() + "\n");
			traverseTreeInorderRecursive(root, resultWriter, 1);
		} else {
			resultWriter.append(root.getFileRef().getName() + "\n");
		}

		return resultWriter.toString();

	}

	private StringWriter traverseTreeInorderRecursive(FileRefNode node, StringWriter resultWriter, int depth) {
		for (FileRefNode child : node.getChildren()) {
			if (child.isDirectory()) {
				resultWriter.append(
						new String(new char[depth * 2]).replace('\0', ' ') + child.getFolder().getName() + "\n");
				traverseTreeInorderRecursive(child, resultWriter, depth + 1);
			} else {
				resultWriter.append(
						new String(new char[depth * 2]).replace('\0', ' ') + child.getFileRef().getName() + "\n");
			}
		}

		return resultWriter;
	}

	@Override
	public String toString() {
		return traverseTreeInorder(root);
	}

}
