package app.dividends.domain.model;

public enum ExcelColumns {
	TICKER("Ticker"),
	CANTIDAD("Cantidad"),
	PRECIOMEDIO("Precio medio"),
	COSTE("Coste total"),
	INGRESOSDIVI("Dividendos Cobrados"),
	PRECIOACTUAL("Precio Actual"),
	VALORACTUAL("Valor Actual"),
	VALORDIVI("Valor mas Dividendos"),
	RENTABILIDAD("Rentabilidad total"),
	RENTABILIDADSINDIVI("Rentabilidad sin dividendo");
	
	private final String nombreColumna;
	
	ExcelColumns(String nombreColumna) {
		this.nombreColumna = nombreColumna;
	}
	
	public String getNombre() {
		return this.nombreColumna;
	}
	
    private static final String[] HEADERS;

    static {
        ExcelColumns[] valores = ExcelColumns.values();
        HEADERS = new String[valores.length];
        for (int i = 0; i < valores.length; i++) {
            HEADERS[i] = valores[i].getNombre();
        }
    }

    public static String[] getHeaders() {
        return HEADERS.clone(); 
    }
}
