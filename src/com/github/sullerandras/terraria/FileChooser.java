package com.github.sullerandras.terraria;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class FileChooser extends JPanel {
    private File folder;
    private final JList<CustomFile> fileList;

    public FileChooser(String folder) {
        super();
        this.setLayout(new GridBagLayout());
        this.folder = new File(folder);

        JTextField filter = new JTextField();
        filter.setToolTipText("Filter files (press enter to apply filter)");
        this.add(filter, UITools.constraints(0, 0, true, false, GridBagConstraints.NORTH));

        fileList = new JList<>();
        fileList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        this.add(new JScrollPane(fileList), UITools.constraints(0, 1, true, true, GridBagConstraints.NORTHWEST));
        this.setMinimumSize(new Dimension(200, 200));

        filter.addActionListener(e -> {
            refreshFiles(filter.getText());
        });
        refreshFiles(filter.getText());
    }

    private static class CustomFile {
        private final File file;
        private final String text;

        public CustomFile(File file) {
            this.file = file;
            this.text = file.getName() + (file.isDirectory() ? "/" : "");
        }

        public String toString() {
            return text;
        }
    }

    public interface FileSelectionListener {
        void fileSelected(File file);
    }

    private void refreshFiles(String filenameFilter) {
        final String lowercaseFilenameFilter = filenameFilter.toLowerCase();
        File[] files = folder.listFiles(pathname -> pathname.getName().toLowerCase().contains(lowercaseFilenameFilter));
        if (files == null) {
            files = new File[0];
        }
        Arrays.sort(files, (f1, f2) -> {
            if (f1.isDirectory() != f2.isDirectory()) {
                return -Boolean.compare(f1.isDirectory(), f2.isDirectory());
            }
            return f1.getName().compareTo(f2.getName());
        });
        final CustomFile[] customFiles = new CustomFile[files.length];
        for (int i = 0; i < files.length; i++) {
            customFiles[i] = new CustomFile(files[i]);
        }
        fileList.setListData(customFiles);
    }

    public void addFileSelectionListener(FileSelectionListener listener) {
        fileList.addListSelectionListener(new ListSelectionListener() {
            private File lastFile = null;

            @Override
            public void valueChanged(ListSelectionEvent e) {
                CustomFile f = fileList.getSelectedValue();
                if (f == null || !f.file.isFile()) {
                    return;
                }
                if (lastFile != f.file) { // avoid calling the listener multiple times with the same value
                    lastFile = f.file;
                    listener.fileSelected(f.file);
                }
            }
        });
    }

    public java.util.List<File> getSelectedFiles() {
        java.util.List<CustomFile> selectedFiles = fileList.getSelectedValuesList();
        java.util.List<File> files = new ArrayList<>(selectedFiles.size());
        for (CustomFile f : selectedFiles) {
            if (f.file.isFile()) {
                files.add(f.file);
            }
        }
        return files;
    }

    public java.util.List<File> getAllFilesRecursively() throws IOException {
        java.util.List<File> files = new ArrayList<>();
        Files.walk(Paths.get(folder.getAbsolutePath())).filter(Files::isRegularFile).forEach(f -> { files.add(f.toFile()); });
        return files;
    }

    public File getFolder() {
        return folder;
    }
}
