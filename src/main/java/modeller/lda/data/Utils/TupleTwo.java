package modeller.lda.data.Utils;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * Created by xschen on 4/5/2017.
 */
@NoArgsConstructor
@Data
public class TupleTwo<T, X> {
    private T v1 = null;
    private X v2 = null;

    public TupleTwo(T v1, X v2) {
        this.v1 = v1;
        this.v2 = v2;
    }

    public T _1(){
        return v1;
    }

    public X _2(){
        return v2;
    }


    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        TupleTwo<?, ?> tupleTwo = (TupleTwo<?, ?>) o;

        if (!Objects.equals(v1, tupleTwo.v1))
            return false;
        return Objects.equals(v2, tupleTwo.v2);

    }


    @Override public int hashCode() {
        int result = v1 != null ? v1.hashCode() : 0;
        result = 31 * result + (v2 != null ? v2.hashCode() : 0);
        return result;
    }

}
