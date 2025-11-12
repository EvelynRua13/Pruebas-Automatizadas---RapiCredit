# language: en
Feature: RapiCredit - Main use cases
  As a RapiCredit user
  I want to perform key actions on the website
  So that core business flows are verified

  Background:
    Given the user is on the Rapicredit home page

  @login
  Scenario: Usuario inicia sesión desde la página principal
    Given the user is on the home page
    When the user clicks "Iniciar sesión"
    When the user enters email "everua77@gmail.com" and password "Megumihonnie78"
    When the user submits the login form
    Then the user should see an indicator "logged_in_indicator"

  @login_fail
  Scenario: Usuario intenta iniciar sesión con contraseña incorrecta y acepta el banner de fallo
    Given the user is on the home page
    When the user clicks "Iniciar sesión"
    When the user enters email "everua77@gmail.com" and password "Honniemegumi78"
    When the user submits the login form and accepts failure banner
    Then the test shows a success banner

  @register
  Scenario: Registrar usuario completo
    Given the user is on the home page
    When the user clicks "Registrar"
    And the user fills the registration form with:
      | identificaciónNúmero  | primerNombre| primerApellido| email              | reEmail           | móvil      | contraseña      | rePassword       |
      | 1002394599           | Cristo      | Rua            | bts1399@gmail.com | bts1399@gmail.com | 3169054256 | Megumihonnie78  | Megumihonnie78   |
    And the user submits the register form
    Then the user should see an indicator "registered_indicator"


  @contact_form
  Scenario: Enviar formulario de contacto con datos válidos
    Given the user is on the Rapicredit home page
    When the user submits the contact form with the following data
      | Nombre        | Email                 | Cedula   | Celular    | Mensaje             |
      | Katherine Rua | bts1306@gmail.com | 1008456857 | 3169054299 | Prueba Automatizada |
    Then the contact message should be sent successfully

  @faq
  Scenario: Buscar en preguntas frecuentes por palabra clave
    Given the user is on the Rapicredit home page
    When the user opens the "Preguntas frecuentes" section
    And the user searches for keyword "Pago"
    Then results related to "Pago" should be visible

  @pay_credit
  Scenario: Pagar un crédito pendiente
    Given the user is on the Rapicredit home page
    When the user clicks "Paga tu crédito"
    And the user enters id "1001138771"
    Then the user should see payment page or message "No hay créditos pendientes"
