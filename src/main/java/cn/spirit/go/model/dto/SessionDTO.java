package cn.spirit.go.model.dto;

import cn.spirit.go.common.enums.UserIdentity;

public class SessionDTO {

    public Integer id;

    public UserIdentity identity;

    public String username;

    public String nickname;

    public Integer source;

}
