package ee.ria.govsso.enduserselfservice.util;

import lombok.experimental.UtilityClass;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;

@UtilityClass
public class TimeUtil {

    public OffsetDateTime toOffsetDateTime(Date date) {
        return OffsetDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }
}
