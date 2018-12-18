package cz.jh.sos.controller;

import cz.jh.sos.model.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
public class CustomerController {

    static final int PAGE_SIZE = 3;

    JdbcTemplate jdbcTemplate;

    public CustomerController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/test")
    public String test() {
        return "Hello world!";
    }

    @GetMapping("/customer/{id}")
    public Customer getCustomer(@PathVariable Long id) {
        return jdbcTemplate.queryForObject(
                "SELECT id, name, city, grade FROM customer WHERE id = ?",
                new Object[]{id},
                new BeanPropertyRowMapper<>(Customer.class));
    }

    @GetMapping("/customer")
    public List<Customer> getCustomers(
            @RequestParam(required = false, defaultValue = "1") Integer pageNo) {
        return jdbcTemplate.query(
                "SELECT id, name, city, grade FROM customer ORDER BY id LIMIT ?,?",
                new Object[]{getHowMuchRowsToSkip(pageNo), PAGE_SIZE},
                new BeanPropertyRowMapper<>(Customer.class));
    }

    @PostMapping("/customer")
    public Customer createCustomer(@RequestBody Customer customer) {
        return customer;
    }

    private int getHowMuchRowsToSkip(int pageNo) {
        if (pageNo <= 0) {
            throw new IllegalArgumentException("invalid number of page");
        }

        return (pageNo - 1) * PAGE_SIZE;
    }

}
