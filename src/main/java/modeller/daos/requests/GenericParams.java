package modeller.daos.requests;

import lombok.Data;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

@Data
public class GenericParams {


    private Date dateStart = null;
    private Date dateEnd = null;
    private String blogger = null;
    private int page = 0;
    private int size = 10;

    public GenericParams() throws ParseException {
        dateStart = new SimpleDateFormat("yyyy-MM-dd").parse("2000-01-01");
        dateEnd = new SimpleDateFormat("yyyy-MM-dd").parse(LocalDate.now().toString());
    }

    public GenericParams(String dateStart, String dateEnd) throws ParseException {
        this.dateStart = new SimpleDateFormat("yyyy-MM-dd").parse(dateStart);
        this.dateEnd = new SimpleDateFormat("yyyy-MM-dd").parse(dateEnd);
    }

    public void setDateStart(String dateStart) throws ParseException {
        this.dateStart = new SimpleDateFormat("yyyy-MM-dd").parse(dateStart);
    }

    public void setDateEnd(String dateEnd) throws ParseException {
        this.dateEnd = new SimpleDateFormat("yyyy-MM-dd").parse(dateEnd);
    }

    public String getStringDateStart() {
        if (dateStart == null) return "";
        DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-DD");
        return dateFormat.format(dateStart);
    }

    public String getStringDateEnd() {
        if (dateEnd == null) return "";
        DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-DD");
        return dateFormat.format(dateEnd);
    }

    public void setPage(String param) {
        if (param == null || param.equals("")) {
            return;
        }
        page = Integer.parseInt(param);

    }

    public void setSize(String param) {
        if (param == null || param.equals("")) {
            return;
        }
        size = Integer.parseInt(param);

    }

    public void setPage(int page) {
        this.page = page;
    }

    public void setSize(int size) {
        this.size = size;


    }
}
