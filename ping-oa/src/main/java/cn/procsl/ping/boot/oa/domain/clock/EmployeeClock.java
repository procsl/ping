package cn.procsl.ping.boot.oa.domain.clock;

import cn.procsl.ping.boot.common.jpa.RepositoryCreator;
import lombok.*;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

@Getter
@Setter
@Entity
@ToString
@RepositoryCreator
@Table(name = "o_employee_clock")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmployeeClock extends AbstractPersistable<Long> implements Serializable {

    @Column(updatable = false, length = 10)
    String employeeId;

    @Column(updatable = false, length = 8)
    String clockDay;

    Date startAt;

    Date finishAt;

    public EmployeeClock(String employeeId) {
        this.employeeId = employeeId;
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
        this.clockDay = fmt.format(new Date());
    }

    public Date start() {
        this.startAt = new Date();
        return this.startAt;
    }

    public Date end() {
        this.finishAt = new Date();
        return this.finishAt;
    }

    int parseHours(Date time) {
        SimpleDateFormat format = new SimpleDateFormat("HH");
        String hourString = format.format(time);
        return Integer.parseInt(hourString);
    }

    public void autoSign(int startHours, int endHours) {
        int now = parseHours(new Date());
        if (now <= startHours && this.startAt == null) {
            this.start();
            return;
        }
        if (now < endHours) {
            if (this.startAt == null) {
                this.start();
                return;
            }
        }

        this.end();
    }

    @Transient
    public boolean isClocked() {
        return this.startAt != null && this.finishAt != null;
    }


}
