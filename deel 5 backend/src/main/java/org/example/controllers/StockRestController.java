package org.example.controllers;


import jakarta.validation.Valid;
import org.example.domain.Stock;
import org.example.formdata.AddStockDto;
import org.example.formdata.ProductionData;
import org.example.formdata.StockDto;
import org.example.service.LifeIsBananasServiceimpl;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping(path = "/stocks", produces = "application/json")
@CrossOrigin(
        origins = {
                "http://localhost:8889",
                "http://localhost:8083"
        },
        allowCredentials = "true"
)
public class StockRestController {

    private final LifeIsBananasServiceimpl lifeIsBananasServiceimpl;

    public StockRestController(LifeIsBananasServiceimpl lifeIsBananasServiceimpl) {
        this.lifeIsBananasServiceimpl = lifeIsBananasServiceimpl;
    }


    @GetMapping("all")
    public List<StockDto> GetAllStocks(){
        return lifeIsBananasServiceimpl.GetAllStock();
    }

    @PostMapping("add/{id}")
    public StockDto AddStock(@PathVariable int id, @RequestBody AddStockDto addStockDto){
        return lifeIsBananasServiceimpl.addStock(id, addStockDto.getAmount());
    }
    @PostMapping("/use")
    public void useStock(@Valid @RequestBody ProductionData productionData) {

        lifeIsBananasServiceimpl.useStock(productionData);
    }


}
