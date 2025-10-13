package com.och.common.xmlcurl.group;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.och.common.xmlcurl.user.Users;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Group implements Serializable {

    @JacksonXmlProperty(localName = "users", isAttribute = true)
    private Users users;
}
