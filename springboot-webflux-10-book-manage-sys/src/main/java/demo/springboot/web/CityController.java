package demo.springboot.web;

import demo.springboot.domain.City;
import demo.springboot.service.CityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import reactor.core.publisher.Mono;

/**
 * city 控制层
 * <p>
 * Created by bysocket
 */
@Controller
@RequestMapping(value = "/city")
public class CityController {

    private static final String CITY_FORM_PATH_NAME = "cityForm";
    private static final String CITY_LIST_PATH_NAME = "cityList";
    private static final String REDIRECT_TO_CITY_URL = "redirect:/city";

    @Autowired
    CityService cityService;

    @RequestMapping(method = RequestMethod.GET)
    public Mono<String> getCityList(final Model model) {
        return cityService.findAll().collectList()
                .doOnNext(cityList -> model.addAttribute("cityList", cityList))
                .thenReturn(CITY_LIST_PATH_NAME);
    }

    @RequestMapping(value = "/create", method = RequestMethod.GET)
    public String createCityForm(final Model model) {
        model.addAttribute("city", new City());
        model.addAttribute("action", "create");
        return CITY_FORM_PATH_NAME;
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public Mono<String> postCity(@ModelAttribute City city) {
        return cityService.insertByCity(city).thenReturn(REDIRECT_TO_CITY_URL);
    }

    @RequestMapping(value = "/update/{id}", method = RequestMethod.GET)
    public Mono<String> getCity(@PathVariable Long id, final Model model) {
        return cityService.findById(id)
                .doOnNext(city -> model.addAttribute("city", city))
                .then(Mono.fromRunnable(() -> model.addAttribute("action", "update")))
                .thenReturn(CITY_FORM_PATH_NAME);
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public Mono<String> putCity(@ModelAttribute City city) {
        return cityService.update(city).thenReturn(REDIRECT_TO_CITY_URL);
    }

    @RequestMapping(value = "/delete/{id}", method = RequestMethod.GET)
    public Mono<String> deleteCity(@PathVariable Long id) {
        return cityService.delete(id).thenReturn(REDIRECT_TO_CITY_URL);
    }

}
