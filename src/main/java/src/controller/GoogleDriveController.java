package src.controller;


import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;


import java.io.IOException;
import java.io.OutputStream;

import java.util.List;


@RestController
@RequestMapping("/google-drive")
public class GoogleDriveController {

    @Autowired
    private Drive driveService;
    // Get all files
    @GetMapping("/files")
    public List<File> getAllGoogleDriveFiles() throws IOException {
        return driveService.files().list()
                .setFields("nextPageToken, files(id, name, parents, mimeType)")
                .execute()
                .getFiles();
    }
    // Create new folder
    @PostMapping("/create-folder")
    public String createNewFolder(@RequestParam String folderName) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setName(folderName);
        fileMetadata.setMimeType("application/vnd.google-apps.folder");

        File file = driveService.files().create(fileMetadata).setFields("id").execute();
        return file.getId();
    }
    // Delete file
    @DeleteMapping("/delete-file/{fileId}")
    public void deleteFile(@PathVariable String fileId) throws IOException {
        driveService.files().delete(fileId).execute();
    }
    // Download file
    @GetMapping("/download-file/{fileId}")
    public void downloadFile(@PathVariable String fileId, OutputStream outputStream) throws IOException {
        driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);
    }



}

