package cn.spirit.go.service.sys;

import cn.spirit.go.common.enums.UploadBucket;
import io.vertx.core.Future;
import io.vertx.core.file.FileSystem;
import io.vertx.ext.web.FileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileStorageSystem {

    private static final Logger log = LoggerFactory.getLogger(FileStorageSystem.class);

    private final FileSystem system;

    private final String storagePath;

    public FileStorageSystem(FileSystem system, String storagePath) {
        this.system = system;
        this.storagePath = storagePath;
    }

    /***
     * 上传文件，将上传的临时文件保存到对应文件夹
     * @param bucket    桶
     * @param file      临时文件
     * @return          新文件名称
     */
    public Future<String> upload(UploadBucket bucket, FileUpload file) {
        String filename = file.uploadedFileName().substring(14) + file.fileName().substring(file.fileName().length() - 4);
        return system.move(file.uploadedFileName(), storagePath + "/" + bucket.name() + "/" + filename)
                .compose(res -> {
                    log.info("Upload File Completed, filename = {}", storagePath + "/" + bucket.name() + "/" + filename);
                    return Future.succeededFuture(filename);
                });
    }

    public void delete(UploadBucket bucket, String filename) {
        String file = storagePath + "/" + bucket.name() + "/" + filename;
        system.delete(file)
                .onSuccess(res -> log.info("Delete file successfully, filename = {}", file))
                .onFailure(res -> log.error("Delete file failed, filename = {} ", file));
    }

}
