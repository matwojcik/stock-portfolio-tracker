# Stock portfolio tracker

Playground scala based application for tracking stock portfolio performance and tax calculation.

## Modules

Despite being single app, strong modularisation has been introduced to better separate concerns and to make it easier in future to split the app into bounded context related services. 

- [core](core) - core domain types
- [importing](importing) - responsible for import of transactions from external model (e.g. csv)
- [portfolio](portfolio) - managing portfolio of stock holdings
- [prices](prices) - responsible for obtaining and keeping stock prices
- [history](history) - keeping portfolio value in history
- [reporting](reporting) - reporting of portfolio value
- [taxes](taxes) - tax calculations
- [root](src) - cross module features, application entry point
 
## License
<a rel="license" href="http://creativecommons.org/licenses/by-nc-sa/4.0/"><img alt="Creative Commons License" style="border-width:0" src="https://i.creativecommons.org/l/by-nc-sa/4.0/88x31.png" /></a><br />This work is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by-nc-sa/4.0/">Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License</a>.
