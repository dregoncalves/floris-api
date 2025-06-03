package br.com.floris.models;
import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;


public class MetaFinanceira {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	
	String descricao;
	BigDecimal valorObjetivo;
	BigDecimal valorAtual;
	LocalDate prazoFinal;
	Boolean concluida;
	
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getDescricao() {
		return descricao;
	}
	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}
	public BigDecimal getValorObjetivo() {
		return valorObjetivo;
	}
	public void setValorObjetivo(BigDecimal valorObjetivo) {
		this.valorObjetivo = valorObjetivo;
	}
	public BigDecimal getValorAtual() {
		return valorAtual;
	}
	public void setValorAtual(BigDecimal valorAtual) {
		this.valorAtual = valorAtual;
	}
	public LocalDate getPrazoFinal() {
		return prazoFinal;
	}
	public void setPrazoFinal(LocalDate prazoFinal) {
		this.prazoFinal = prazoFinal;
	}
	public Boolean getConcluida() {
		return concluida;
	}
	public void setConcluida(Boolean concluida) {
		this.concluida = concluida;
	}

}
