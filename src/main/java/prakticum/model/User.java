package prakticum.model;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class User {
    private String email;
    private String password;
    private String name;

}
