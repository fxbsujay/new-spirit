package cn.spirit.go.model.entity;

import cn.spirit.go.common.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class UserEntity {

  public String _id;

  /**
   * 用户账号
   */
  public String username;

  /**
   * 用户头像
   */
  public String avatar;

  /**
   * 用户昵称
   */
  public String nickname;

  /**
   * 用户邮箱
   */
  public String email;

  /**
   * 用户状态
   */
  public UserStatus status;

  /**
   * 用户密码
   */
  public String password;

  /**
   * 创建时间
   */
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  public LocalDateTime createdAt;

}
