package aopusingjavaproxy;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class StudentImpl implements IStudent {

    private int id;
    private String name;

    @Override
    public void study() {
        System.out.println("study 10000 hours.");
    }
}
