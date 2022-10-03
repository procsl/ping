package cn.procsl.ping.boot.oa.adapter;

import cn.procsl.ping.boot.common.error.BusinessException;
import cn.procsl.ping.boot.oa.domain.clock.HolidayService;
import cn.procsl.ping.boot.oa.domain.clock.MonthHolidayCalendar;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ApiHubCalendarServiceAdapter implements HolidayService {

    final String calendar_service_api = "https://api.apihubs.cn/holiday/get?field=weekend,workday,holiday_today," +
            "holiday_legal,holiday_recess,date,month&month=%s&order_by=1&cn=1&size=31";


    final RestTemplate restTemplate;

    @Override
    @Cacheable(cacheNames = "MonthHolidayCalendar", key = "#yearAndMonth")
    public Collection<MonthHolidayCalendar> getCalender(String yearAndMonth) {
        String url = String.format(calendar_service_api, yearAndMonth);
        ResponseEntity<ResponseDTO> response = restTemplate.getForEntity(url, ResponseDTO.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new BusinessException("获取接口数据失败:" + response.getStatusCode());
        }
        if (!response.hasBody()) {
            throw new BusinessException("获取接口数据失败: 接口未返回数据");
        }
        ResponseDTO body = response.getBody();
        assert body != null;
        if (!Objects.equals(0, body.code)) {
            throw new BusinessException("获取接口数据失败: 响应数据code错误" + body);
        }
        return body.data.list.stream().map(HolidayDTO::transform).collect(Collectors.toList());
    }

    @Getter
    @Setter
    @ToString
    final static class ResponseDTO {
        Integer code;
        String msg;
        HolidayItemDTO data;
    }

    @Getter
    @Setter
    @ToString
    final static class HolidayItemDTO {
        Collection<HolidayDTO> list;
    }

    @Getter
    @Setter
    @ToString
    final static class HolidayDTO {
        String weekend, workday, holiday_today, holiday_legal, holiday_recess, date, month;

        String weekend_cn, workday_cn, holiday_today_cn, holiday_legal_cn, holiday_recess_cn, date_cn, month_cn;

        public MonthHolidayCalendar transform() {
            MonthHolidayCalendar calendar = new MonthHolidayCalendar();
            calendar.setDay(this.getDate());
            calendar.setWorkday(this.workday_cn);
            calendar.setHolidayRecess(this.holiday_recess_cn);
            calendar.setWeekend(this.weekend_cn);
            calendar.setHolidayToday(this.holiday_today_cn);
            calendar.setHolidayLegal(this.holiday_legal_cn);
            return calendar;
        }
    }


}


