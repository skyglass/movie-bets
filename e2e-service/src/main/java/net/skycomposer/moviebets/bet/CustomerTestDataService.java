package net.skycomposer.moviebets.bet;

import lombok.extern.slf4j.Slf4j;
import net.skycomposer.moviebets.testdata.JdbcTestDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CustomerTestDataService extends JdbcTestDataService {

    @Autowired
    @Qualifier("customerJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    @Override
    protected JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    @Override
    public void resetDatabase() {
        executeString("DELETE FROM customer");
        executeString("DELETE FROM fund_request");
    }

}