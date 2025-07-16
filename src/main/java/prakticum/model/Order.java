package prakticum.model;

import groovy.transform.builder.Builder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Jacksonized
public class Order {
    private List<String> ingredients;
}