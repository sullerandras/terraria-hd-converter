package com.github.sullerandras.terraria;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.io.File;
import java.util.Arrays;

public class FileChooser extends JPanel {
    private String folder;
    private final JList<CustomFile> fileList;

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

    public FileChooser(String folder) {
        super();
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.folder = folder;

        fileList = new JList<>();
        fileList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        this.add(new JScrollPane(fileList));
        this.setMinimumSize(new Dimension(200, 200));

        refreshFiles();
    }

    private void refreshFiles() {
        File[] files = new File(folder).listFiles();
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
}
