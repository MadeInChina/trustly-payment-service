package org.trustly.paymentservice.web;

import com.vdurmont.emoji.EmojiParser;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.trustly.paymentService.trustly.commons.Currency;
import org.trustly.paymentService.trustly.data.request.Request;
import org.trustly.paymentService.trustly.data.response.Response;
import org.trustly.paymentService.trustly.requestbuilders.Deposit;
import org.trustly.paymentService.trustly.requestbuilders.SelectAccount;
import org.trustly.paymentservice.services.trustly.TrustlyService;
import org.trustly.paymentservice.web.dtos.SelectAccountDTO;
import org.trustly.paymentservice.web.dtos.UrlResponse;

@Controller
public class MainController {

  Logger log = LoggerFactory.getLogger(MainController.class);

  TrustlyService service;

  public MainController(TrustlyService service) {
    this.service = service;
  }

  @GetMapping("")
  public ModelAndView index(Map<String, Object> model) {

    String parsed = EmojiParser.parseToUnicode("Sweet :santa:");
    model.put("header", parsed);
    model.put("name", "Tolvan");

    return new ModelAndView("index", model);
  }

  @GetMapping("selectAccount")
  public ModelAndView selectAccount(Model model) {

    String parsed = EmojiParser.parseToUnicode("Sweet :santa:");
    model.addAttribute("header", parsed);
    model.addAttribute("data", new SelectAccountDTO("", "", "", "", "", ""));

    return new ModelAndView("selectAccount").addAllObjects(model.asMap());
  }

  @PostMapping("selectAccount")
  public Object selecAccount(
      @ModelAttribute SelectAccountDTO selectAccount, Map<String, Object> model) {

    final SelectAccount.Build requestBuilder =
        new SelectAccount.Build(
            "https://google.com", selectAccount.getSsn(), UUID.randomUUID().toString());

    final Request request =
        requestBuilder
            .country("SE")
            .ip("127.0.0.1")
            .
            // Currency.SEK,
            firstName(selectAccount.getFirstName())
            .lastName(selectAccount.getLastName())
            .email(selectAccount.getEmail())
            .nationalIdentificationNumber(selectAccount.getSsn())
            .requestDirectDebitMandate("1")
            .locale("sv_SE")
            .getRequest();

    final Response response = this.service.sendRequest(request);

    log.info(response.toString());
    // log.info(response.getResult().toString());

    if (response.successfulResult()) {
      Map<String, Object> data = (Map<String, Object>) response.getResult().getData();
      return new RedirectView((String) data.get("url"));
    }

    String parsed = EmojiParser.parseToUnicode("Sweet :santa:");
    model.put("header", parsed);

    return new ModelAndView("selectAccount", model);
  }

  @PostMapping("createPayment")
  public ResponseEntity<?> createPayment(@RequestBody SelectAccountDTO params) {
    final Deposit.Build requestBuilder =
        new Deposit.Build(
            "https://google.com",
            "19121212-1212",
            UUID.randomUUID().toString(),
            Currency.SEK,
            params.getFirstName(),
            params.getLastName(),
            params.getEmail());

    final Request request =
        requestBuilder
            .country("SE")
            .ip("127.0.0.1")
            .nationalIdentificationNumber(params.getSsn())
            .requestDirectDebitMandate("1")
            .locale("sv_SE")
            .getRequest();

    final Response response = this.service.sendRequest(request);

    log.info(response.toString());

    if (response.successfulResult()) {
      Map<String, Object> data = (Map<String, Object>) response.getResult().getData();
      final UrlResponse urlResponse =
          new UrlResponse((String) data.get("url"), (String) data.get("orderId"));
      return ResponseEntity.ok(urlResponse);
    } else {
      log.error(response.getError().getMessage());
    }

    return ResponseEntity.status(500).build();
  }
}
