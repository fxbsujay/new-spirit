package cn.spirit.go.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class BaseEntity {

  public Integer id;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  public LocalDateTime updatedAt;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  public LocalDateTime createdAt;

}
