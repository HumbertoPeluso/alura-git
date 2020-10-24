
package br.com.confirmeonline.controller;

import java.io.ByteArrayInputStream;
import java.rmi.RemoteException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.rpc.ServiceException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.credsearch.dao.RobotDao;
import com.credsearch.model.Pessoa;
import com.google.common.base.Strings;

import br.com.confirmeonline.dao.PessoaJuridicaDAO;
import br.com.confirmeonline.exception.PpeException;
import br.com.confirmeonline.exception.ProtectedCpfCgcException;
import br.com.confirmeonline.model.Emails;
import br.com.confirmeonline.model.EmpresaVo;
import br.com.confirmeonline.model.EnderecoComercial;
import br.com.confirmeonline.model.Imoveis;
import br.com.confirmeonline.model.Infocomplementares;
import br.com.confirmeonline.model.Moradores;
import br.com.confirmeonline.model.Obito;
import br.com.confirmeonline.model.Parentes;
import br.com.confirmeonline.model.PessoaObitoDTO;
import br.com.confirmeonline.model.Sociedades;
import br.com.confirmeonline.model.SocioSociedade;
import br.com.confirmeonline.model.Socios;
import br.com.confirmeonline.model.Telefone;
import br.com.confirmeonline.model.TelefonesComerciais;
import br.com.confirmeonline.model.TelefonesReferencia;
import br.com.confirmeonline.model.UF;
import br.com.confirmeonline.model.Veiculo;
import br.com.confirmeonline.model.Vizinhos;
import br.com.confirmeonline.persistence.ObitoDao;
import br.com.confirmeonline.persistence.RespostaDAO;
import br.com.confirmeonline.resources.Conexao;
import br.com.confirmeonline.resources.DBConnectionPool;
//import br.com.confirmeonline.service.ConsultaInfoSimplesService;
//import br.com.confirmeonline.service.ConsultaWsReceitaService;
import br.com.confirmeonline.service.ConsultaReceitaService;
import br.com.confirmeonline.util.Constantes;
import br.com.confirmeonline.util.PesquisaCepUtil;
import br.com.confirmeonline.util.SqlToBind;
import br.com.confirmeonline.util.StringUtils;
import br.com.confirmeonline.util.Utils;
import br.com.confirmeonline.vo.Empresa;
import br.com.confirmeonline.webservice.credito.Credito;
import br.com.confirmeonline.webservice.credito.CreditoServiceLocator;
import br.com.credilink.Search;
import oracle.jdbc.OracleTypes;

@ManagedBean
@SessionScoped
public class Resposta {

	private static Logger logger = Logger.getLogger(Resposta.class);

	public Boolean protecaoDados = false;

	public Resposta() {
		// this.connection = getConnection();
	}

	// teste de commit

	@SuppressWarnings("unused")
	private List<Connection> connectionPool = new ArrayList<Connection>();
	private br.com.credilink.Telefone telefoneOperadora;
	private String nomeservidor;
	private String[] cpfcnpjArmazenado = new String[5];

	private List<Emails> emails;
	private List<Telefone> telefone;
	private List<Telefone> fixos;
	private List<Telefone> celulares;
	private List<TelefonesComerciais> telefoneComercial;
	private List<TelefonesReferencia> telefoneReferencia;
	private List<Parentes> parentes;
	private Obito obito;
	private Obito obitoNacional;
	private List<Moradores> moradores;
	private List<Vizinhos> vizinhos;
	private List<Parentes> filhos;
	// private List<Filho> filhos;
	private List<Veiculo> veiculos;
	private List<Imoveis> imoveis;
	private List<Sociedades> sociedades;
	private List<EnderecoComercial> enderecosComerciais;
	private List<Socios> socios;
	private Infocomplementares infocomplementares;
	private String label1; // Dt. nascimento / Dt. fundação
	private String label2; // Signo // Nome Fantasia
	private String label3; // Sexo // Natureza:
	private String label4; // Nome da Mae //Situacao
	private String label5; // Título de Eleitor//
	private String label6; // Nome Pai//
	private String form_active_cpfcnpj;
	private String form_active_telefone;
	private String form_active_obitoNacional;
	private String form_active_operadora;
	private String form_active_endereco;
	private String form_active_cep;
	private String form_active_nome;
	private String form_active_razao_social;
	private String form_active_mapa;
	private String form_active_veiculos;
	private String form_active_historico_credito;
	private String form_active_con_armazenada;
	private String form_active_rede_social;
	private String htmlrespcredito;
	private Boolean paginadadosbasicos = false;
	private Boolean paginadadosbasicosAnt = false;
	private Boolean paginadadosbasicosProx = false;
	private Boolean paginavizinhos = false;
	private Boolean paginavizinhosAnt = false;
	private Boolean paginavizinhosProx = false;
	private Boolean paginaFilhosAnt = false;
	private Boolean paginaFilhosProx = false;
	private Boolean paginaparentes = false;
	private Boolean paginaparentesAnt = false;
	private Boolean paginaparentesProx = false;
	private Boolean paginamoradores = false;
	private Boolean paginamoradoresAnt = false;
	private Boolean paginamoradoresProx = false;
	private Boolean paginasociedades = false;
	private Boolean paginasociedadesAnt = false;
	private Boolean paginasociedadesProx = false;
	private Boolean paginaEnderecosComerciais = false;
	private Boolean paginaEnderecosComerciaisAnt = false;
	private Boolean paginaEnderecosComerciaisProx = false;
	private Boolean paginasocios = false;
	private Boolean paginasociosAnt = false;
	private Boolean paginasociosProx = false;
	private Boolean paginaemail = false;
	private Boolean paginaemailAnt = false;
	private Boolean paginaemailProx = false;
	private Boolean paginatelefonescomerciais = false;
	private Boolean paginatelefonescomerciaisAnt = false;
	private Boolean paginatelefonescomerciaisProx = false;
	private Boolean paginaveiculos = false;
	private Boolean paginaveiculosAnt = false;
	private Boolean paginaveiculosProx = false;
	private Boolean paginatelefonesreferencia = false;
	private Boolean paginatelefonesreferenciaAnt = false;
	private Boolean paginatelefonesreferenciaProx = false;
	private Boolean paginaimoveis = false;
	private Boolean paginaimoveisAnt = false;
	private Boolean paginaimoveisProx = false;
	private Boolean paginacosultaendereco = false;
	private Boolean paginacosultaenderecoAnt = false;
	private Boolean paginacosultaenderecoProx = false;
	private Boolean paginaconsultanome = false;
	private Boolean paginaconsultanomeAnt = false;
	private Boolean paginaconsultanomeProx = false;
	private Boolean paginaconsultacep = false;
	private Boolean paginaconsultacepAnt = false;
	private Boolean paginaconsultacepProx = false;
	private Boolean lastVizinhoCentro;
	private Boolean lastVizinhoEsquerda;
	private Boolean lastVizinhoDireita;
	private Credito credito;
	private String mensagem;
	private String respnaopossuitelcomercial;
	private Boolean exibirMensagem;
	private Boolean possuiConexao = false;
	public Connection connection;
	private RespostaDAO dao = new RespostaDAO();
	private List<String> operadoras = new ArrayList<String>();
	private Boolean bPesquisaDM_Parente = false;
	private Integer tabConArmazenada;
	private String[] armazenados = new String[5];
	private Boolean useStatus;
	private Boolean menorDeIdade=false;
	

	public String getForm_active_rede_social() {
		return form_active_rede_social;
	}

	public void setForm_active_rede_social(String form_active_rede_social) {
		this.form_active_rede_social = form_active_rede_social;
	}

	public String[] getArmazenados() {
		return armazenados;
	}

	public void setArmazenados(String[] armazenados) {
		this.armazenados = armazenados;
	}

	public Integer getTabConArmazenada() {
		return tabConArmazenada;
	}

	public void setTabConArmazenada(Integer tabConArmazenada) {
		this.tabConArmazenada = tabConArmazenada;

	}

	public String[] getCpfcnpjArmazenado() {
		return cpfcnpjArmazenado;
	}

	public void setCpfcnpjArmazenado(String[] cpfcnpjArmazenado) {
		this.cpfcnpjArmazenado = cpfcnpjArmazenado;
	}

	/* Metodos da Classe Bean */

	public Boolean getPaginaEnderecosComerciais() {
		return paginaEnderecosComerciais;
	}

	public Boolean getbPesquisaDM_Parente() {
		return bPesquisaDM_Parente;
	}

	public void setbPesquisaDM_Parente(Boolean bPesquisaDM_Parente) {
		this.bPesquisaDM_Parente = bPesquisaDM_Parente;
	}

	public void setPaginaEnderecosComerciais(Boolean paginaEnderecosComerciais) {
		this.paginaEnderecosComerciais = paginaEnderecosComerciais;
	}

	public Boolean getPaginaEnderecosComerciaisAnt() {
		return paginaEnderecosComerciaisAnt;
	}

	public void setPaginaEnderecosComerciaisAnt(Boolean paginaEnderecosComerciaisAnt) {
		this.paginaEnderecosComerciaisAnt = paginaEnderecosComerciaisAnt;
	}

	public Boolean getPaginaEnderecosComerciaisProx() {
		return paginaEnderecosComerciaisProx;
	}

	public void setPaginaEnderecosComerciaisProx(Boolean paginaEnderecosComerciaisProx) {
		this.paginaEnderecosComerciaisProx = paginaEnderecosComerciaisProx;
	}

	public List<EnderecoComercial> getEnderecosComerciais() {
		return enderecosComerciais;
	}

	public void setEnderecosComerciais(List<EnderecoComercial> enderecosComerciais) {
		this.enderecosComerciais = enderecosComerciais;
	}

	public Boolean getPossuiConexao() {
		return possuiConexao;
	}

	public void setPossuiConexao(Boolean possuiConexao) {
		this.possuiConexao = possuiConexao;
	}

	public String getForm_active_razao_social() {
		return form_active_razao_social;
	}

	public void setForm_active_razao_social(String form_active_razao_social) {
		this.form_active_razao_social = form_active_razao_social;
	}

	public Boolean getProtecaoDados() {
		return protecaoDados;
	}

	public void setProtecaoDados(Boolean protecaoDados) {
		this.protecaoDados = protecaoDados;
	}
	
	

	public Boolean getMenorDeIdade() {
		return menorDeIdade;
	}

	public void setMenorDeIdade(Boolean menorDeIdade) {
		this.menorDeIdade = menorDeIdade;
	}

	public Connection getConnection() {
		try {
			if (!possuiConexao || (null == this.connection)) {
				this.connection = DBConnectionPool.getConnectionDBCred();
				// connectionPool.add(connection);
			} else {
				if (connection.isClosed()) {
					this.connection = DBConnectionPool.getConnectionDBCred();
				}
			}
		} catch (Exception e) {
			logger.error("Erro no metodo getConnection da classe resposta: " + e.getMessage());

		}
		return connection;
	}

	public void releaseConnection() {
		if (!possuiConexao) {
			// Iterator<Connection> it = connectionPool.iterator();
			// while(it.hasNext()){
			// try{
			// DBConnectionPool.releaseConnection(it.next());
			// }catch(Exception ex){}
			// }
			// connectionPool.clear();
			DBConnectionPool.releaseConnection(connection);
		}
	}

	public void setConnection(Connection conn) {
		this.connection = conn;
	}

	public Obito getObitoNacional() {
		return obitoNacional;
	}

	public void setObitoNacional(Obito obitoNacional) {
		this.obitoNacional = obitoNacional;
	}

	public Boolean getExibirMensagem() {
		return exibirMensagem;
	}

	public void setExibirMensagem(Boolean exibirMensagem) {
		this.exibirMensagem = exibirMensagem;
	}

	public Boolean getLastVizinhoCentro() {
		return lastVizinhoCentro;
	}

	public void setLastVizinhoCentro(Boolean lastVizinhoCentro) {
		this.lastVizinhoCentro = lastVizinhoCentro;
	}

	public String getForm_active_obitoNacional() {
		return form_active_obitoNacional;
	}

	public void setForm_active_obitoNacional(String form_active_obitoNacional) {
		this.form_active_obitoNacional = form_active_obitoNacional;
	}

	public Boolean getLastVizinhoEsquerda() {
		return lastVizinhoEsquerda;
	}

	public void setLastVizinhoEsquerda(Boolean lastVizinhoEsquerda) {
		this.lastVizinhoEsquerda = lastVizinhoEsquerda;
	}

	public Boolean getLastVizinhoDireita() {
		return lastVizinhoDireita;
	}

	public void setLastVizinhoDireita(Boolean lastVizinhoDireita) {
		this.lastVizinhoDireita = lastVizinhoDireita;
	}

	public Boolean getPaginaFilhosAnt() {
		return paginaFilhosAnt;
	}

	public void setPaginaFilhosAnt(Boolean paginaFilhosAnt) {
		this.paginaFilhosAnt = paginaFilhosAnt;
	}

	public Boolean getPaginaFilhosProx() {
		return paginaFilhosProx;
	}

	public void setPaginaFilhosProx(Boolean paginaFilhosProx) {
		this.paginaFilhosProx = paginaFilhosProx;
	}

	public List<Parentes> getFilhos() {
		return filhos;
	}

	public void setFilhos(List<Parentes> filhos) {
		this.filhos = filhos;
	}

	public Boolean getPaginavizinhosAnt() {
		return paginavizinhosAnt;
	}

	public void setPaginavizinhosAnt(Boolean paginavizinhosAnt) {
		this.paginavizinhosAnt = paginavizinhosAnt;
	}

	public Boolean getPaginavizinhosProx() {
		return paginavizinhosProx;
	}

	public void setPaginavizinhosProx(Boolean paginavizinhosProx) {
		this.paginavizinhosProx = paginavizinhosProx;
	}

	public Boolean getPaginaparentesAnt() {
		return paginaparentesAnt;
	}

	public void setPaginaparentesAnt(Boolean paginaparentesAnt) {
		this.paginaparentesAnt = paginaparentesAnt;
	}

	public Boolean getPaginaparentesProx() {
		return paginaparentesProx;
	}

	public void setPaginaparentesProx(Boolean paginaparentesProx) {
		this.paginaparentesProx = paginaparentesProx;
	}

	public Boolean getPaginamoradoresAnt() {
		return paginamoradoresAnt;
	}

	public void setPaginamoradoresAnt(Boolean paginamoradoresAnt) {
		this.paginamoradoresAnt = paginamoradoresAnt;
	}

	public Boolean getPaginamoradoresProx() {
		return paginamoradoresProx;
	}

	public void setPaginamoradoresProx(Boolean paginamoradoresProx) {
		this.paginamoradoresProx = paginamoradoresProx;
	}

	public Boolean getPaginasociedadesAnt() {
		return paginasociedadesAnt;
	}

	public void setPaginasociedadesAnt(Boolean paginasociedadesAnt) {
		this.paginasociedadesAnt = paginasociedadesAnt;
	}

	public Boolean getPaginasociedadesProx() {
		return paginasociedadesProx;
	}

	public void setPaginasociedadesProx(Boolean paginasociedadesProx) {
		this.paginasociedadesProx = paginasociedadesProx;
	}

	public Boolean getPaginasociosAnt() {
		return paginasociosAnt;
	}

	public void setPaginasociosAnt(Boolean paginasociosAnt) {
		this.paginasociosAnt = paginasociosAnt;
	}

	public Boolean getPaginasociosProx() {
		return paginasociosProx;
	}

	public void setPaginasociosProx(Boolean paginasociosProx) {
		this.paginasociosProx = paginasociosProx;
	}

	public Boolean getPaginaemailAnt() {
		return paginaemailAnt;
	}

	public void setPaginaemailAnt(Boolean paginaemailAnt) {
		this.paginaemailAnt = paginaemailAnt;
	}

	public Boolean getPaginaemailProx() {
		return paginaemailProx;
	}

	public void setPaginaemailProx(Boolean paginaemailProx) {
		this.paginaemailProx = paginaemailProx;
	}

	public Boolean getPaginatelefonescomerciaisAnt() {
		return paginatelefonescomerciaisAnt;
	}

	public void setPaginatelefonescomerciaisAnt(Boolean paginatelefonescomerciaisAnt) {
		this.paginatelefonescomerciaisAnt = paginatelefonescomerciaisAnt;
	}

	public Boolean getPaginatelefonescomerciaisProx() {
		return paginatelefonescomerciaisProx;
	}

	public void setPaginatelefonescomerciaisProx(Boolean paginatelefonescomerciaisProx) {
		this.paginatelefonescomerciaisProx = paginatelefonescomerciaisProx;
	}

	public Boolean getPaginaveiculosAnt() {
		return paginaveiculosAnt;
	}

	public void setPaginaveiculosAnt(Boolean paginaveiculosAnt) {
		this.paginaveiculosAnt = paginaveiculosAnt;
	}

	public Boolean getPaginaveiculosProx() {
		return paginaveiculosProx;
	}

	public void setPaginaveiculosProx(Boolean paginaveiculosProx) {
		this.paginaveiculosProx = paginaveiculosProx;
	}

	public Boolean getPaginatelefonesreferenciaAnt() {
		return paginatelefonesreferenciaAnt;
	}

	public void setPaginatelefonesreferenciaAnt(Boolean paginatelefonesreferenciaAnt) {
		this.paginatelefonesreferenciaAnt = paginatelefonesreferenciaAnt;
	}

	public Boolean getPaginatelefonesreferenciaProx() {
		return paginatelefonesreferenciaProx;
	}

	public void setPaginatelefonesreferenciaProx(Boolean paginatelefonesreferenciaProx) {
		this.paginatelefonesreferenciaProx = paginatelefonesreferenciaProx;
	}

	public Boolean getPaginaimoveisAnt() {
		return paginaimoveisAnt;
	}

	public void setPaginaimoveisAnt(Boolean paginaimoveisAnt) {
		this.paginaimoveisAnt = paginaimoveisAnt;
	}

	public Boolean getPaginaimoveisProx() {
		return paginaimoveisProx;
	}

	public void setPaginaimoveisProx(Boolean paginaimoveisProx) {
		this.paginaimoveisProx = paginaimoveisProx;
	}

	public Boolean getPaginacosultaenderecoAnt() {
		return paginacosultaenderecoAnt;
	}

	public void setPaginacosultaenderecoAnt(Boolean paginacosultaenderecoAnt) {
		this.paginacosultaenderecoAnt = paginacosultaenderecoAnt;
	}

	public Boolean getPaginacosultaenderecoProx() {
		return paginacosultaenderecoProx;
	}

	public void setPaginacosultaenderecoProx(Boolean paginacosultaenderecoProx) {
		this.paginacosultaenderecoProx = paginacosultaenderecoProx;
	}

	public Boolean getPaginaconsultanomeAnt() {
		return paginaconsultanomeAnt;
	}

	public void setPaginaconsultanomeAnt(Boolean paginaconsultanomeAnt) {
		this.paginaconsultanomeAnt = paginaconsultanomeAnt;
	}

	public Boolean getPaginaconsultanomeProx() {
		return paginaconsultanomeProx;
	}

	public void setPaginaconsultanomeProx(Boolean paginaconsultanomeProx) {
		this.paginaconsultanomeProx = paginaconsultanomeProx;
	}

	public Boolean getPaginaconsultacepAnt() {
		return paginaconsultacepAnt;
	}

	public void setPaginaconsultacepAnt(Boolean paginaconsultacepAnt) {
		this.paginaconsultacepAnt = paginaconsultacepAnt;
	}

	public Boolean getPaginaconsultacepProx() {
		return paginaconsultacepProx;
	}

	public void setPaginaconsultacepProx(Boolean paginaconsultacepProx) {
		this.paginaconsultacepProx = paginaconsultacepProx;
	}

	public Boolean getPaginadadosbasicosAnt() {
		return paginadadosbasicosAnt;
	}

	public void setPaginadadosbasicosAnt(Boolean paginadadosbasicosAnt) {
		this.paginadadosbasicosAnt = paginadadosbasicosAnt;
	}

	public Boolean getPaginadadosbasicosProx() {
		return paginadadosbasicosProx;
	}

	public void setPaginadadosbasicosProx(Boolean paginadadosbasicosProx) {
		this.paginadadosbasicosProx = paginadadosbasicosProx;
	}

	public br.com.credilink.Telefone getTelefoneOperadora() {
		return telefoneOperadora;
	}

	public void setTelefoneOperadora(br.com.credilink.Telefone telefoneOperadora) {
		this.telefoneOperadora = telefoneOperadora;
	}

	public String getForm_active_operadora() {
		return form_active_operadora;
	}

	public void setForm_active_operadora(String form_active_operadora) {
		this.form_active_operadora = form_active_operadora;
	}

	public String getNomeservidor() {
		return nomeservidor;
	}

	public void setNomeservidor(String nomeservidor) {
		this.nomeservidor = nomeservidor;
	}

	public String getRespnaopossuitelcomercial() {
		return respnaopossuitelcomercial;
	}

	public void setRespnaopossuitelcomercial(String respnaopossuitelcomercial) {
		this.respnaopossuitelcomercial = respnaopossuitelcomercial;
	}

	public String getHtmlrespcredito() {
		return htmlrespcredito;
	}

	public void setHtmlrespcredito(String htmlrespcredito) {
		this.htmlrespcredito = htmlrespcredito;
	}

	public String getMensagem() {
		return mensagem;
	}

	public void setMensagem(String mensagem) {
		this.mensagem = mensagem;
	}

	public Credito getCredito() {
		return credito;
	}

	public void setCredito(Credito credito) {
		this.credito = credito;
	}

	public Boolean getPaginasocios() {
		return paginasocios;
	}

	public void setPaginasocios(Boolean paginasocios) {
		this.paginasocios = paginasocios;
	}

	public List<Socios> getSocios() {
		return socios;
	}

	public void setSocios(List<Socios> socios) {
		this.socios = socios;
	}

	public Boolean getPaginadadosbasicos() {
		return paginadadosbasicos;
	}

	public void setPaginadadosbasicos(Boolean paginadadosbasicos) {
		this.paginadadosbasicos = paginadadosbasicos;
	}

	public Boolean getPaginavizinhos() {
		return paginavizinhos;
	}

	public void setPaginavizinhos(Boolean paginavizinhos) {
		this.paginavizinhos = paginavizinhos;
	}

	public Boolean getPaginaparentes() {
		return paginaparentes;
	}

	public void setPaginaparentes(Boolean paginaparentes) {
		this.paginaparentes = paginaparentes;
	}

	public Boolean getPaginamoradores() {
		return paginamoradores;
	}

	public void setPaginamoradores(Boolean paginamoradores) {
		this.paginamoradores = paginamoradores;
	}

	public Boolean getPaginasociedades() {
		return paginasociedades;
	}

	public void setPaginasociedades(Boolean paginasociedades) {
		this.paginasociedades = paginasociedades;
	}

	public Boolean getPaginaemail() {
		return paginaemail;
	}

	public void setPaginaemail(Boolean paginaemail) {
		this.paginaemail = paginaemail;
	}

	public Boolean getPaginatelefonescomerciais() {
		return paginatelefonescomerciais;
	}

	public void setPaginatelefonescomerciais(Boolean paginatelefonescomerciais) {
		this.paginatelefonescomerciais = paginatelefonescomerciais;
	}

	public Boolean getPaginaveiculos() {
		return paginaveiculos;
	}

	public void setPaginaveiculos(Boolean paginaveiculos) {
		this.paginaveiculos = paginaveiculos;
	}

	public Boolean getPaginatelefonesreferencia() {
		return paginatelefonesreferencia;
	}

	public void setPaginatelefonesreferencia(Boolean paginatelefonesreferencia) {
		this.paginatelefonesreferencia = paginatelefonesreferencia;
	}

	public Boolean getPaginaimoveis() {
		return paginaimoveis;
	}

	public void setPaginaimoveis(Boolean paginaimoveis) {
		this.paginaimoveis = paginaimoveis;
	}

	public Boolean getPaginacosultaendereco() {
		return paginacosultaendereco;
	}

	public void setPaginacosultaendereco(Boolean paginacosultaendereco) {
		this.paginacosultaendereco = paginacosultaendereco;
	}

	public Boolean getPaginaconsultanome() {
		return paginaconsultanome;
	}

	public void setPaginaconsultanome(Boolean paginaconsultanome) {
		this.paginaconsultanome = paginaconsultanome;
	}

	public Boolean getPaginaconsultacep() {
		return paginaconsultacep;
	}

	public void setretornaOperadorasSelecionadas(Boolean paginaconsultacep) {
		this.paginaconsultacep = paginaconsultacep;
	}

	public List<Emails> getEmails() {
		return emails;
	}

	public void setEmails(List<Emails> emails) {
		this.emails = emails;
	}

	public List<Sociedades> getSociedades() {
		return sociedades;
	}

	public void setSociedades(List<Sociedades> sociedades) {
		this.sociedades = sociedades;
	}

	public String getForm_active_cpfcnpj() {
		return form_active_cpfcnpj;
	}

	public void setForm_active_cpfcnpj(String form_active_cpfcnpj) {
		this.form_active_cpfcnpj = form_active_cpfcnpj;
	}

	public String getForm_active_con_armazenada() {
		return form_active_con_armazenada;
	}

	public void setForm_active_con_armazenada(String form_active_con_armazenada) {
		this.form_active_con_armazenada = form_active_con_armazenada;
	}

	public String getForm_active_telefone() {
		return form_active_telefone;
	}

	public void setForm_active_telefone(String form_active_telefone) {
		this.form_active_telefone = form_active_telefone;
	}

	public String getForm_active_endereco() {
		return form_active_endereco;
	}

	public void setForm_active_endereco(String form_active_endereco) {
		this.form_active_endereco = form_active_endereco;
	}

	public String getForm_active_cep() {
		return form_active_cep;
	}

	public void setForm_active_cep(String form_active_cep) {
		this.form_active_cep = form_active_cep;
	}

	public String getForm_active_nome() {
		return form_active_nome;
	}

	public void setForm_active_nome(String form_active_nome) {
		this.form_active_nome = form_active_nome;
	}

	public String getForm_active_veiculos() {
		return form_active_veiculos;
	}

	public String getForm_active_mapa() {
		return form_active_mapa;
	}

	public void setForm_active_mapa(String form_active_mapa) {
		this.form_active_mapa = form_active_mapa;
	}

	public void setForm_active_veiculos(String form_active_veiculos) {
		this.form_active_veiculos = form_active_veiculos;
	}

	public String getForm_active_historico_credito() {
		return form_active_historico_credito;
	}

	public void setForm_active_historico_credito(String form_active_historico_credito) {
		this.form_active_historico_credito = form_active_historico_credito;
	}

	public String getLabel3() {
		return label3;
	}

	public void setLabel3(String label3) {
		this.label3 = label3;
	}

	public String getLabel5() {
		return label5;
	}

	public void setLabel5(String label5) {
		this.label5 = label5;
	}

	public String getLabel4() {
		return label4;
	}

	public void setLabel4(String label4) {
		this.label4 = label4;
	}

	public String getLabel1() {
		return label1;
	}

	public void setLabel1(String label1) {
		this.label1 = label1;
	}

	public String getLabel2() {
		return label2;
	}

	public void setLabel2(String label2) {
		this.label2 = label2;
	}

	public List<Telefone> getFixos() {
		return fixos;
	}

	public void setFixos(List<Telefone> fixos) {
		this.fixos = fixos;
	}

	public List<Telefone> getCelulares() {
		return celulares;
	}

	public void setCelulares(List<Telefone> celulares) {
		this.celulares = celulares;
	}

	public Resposta(List<Telefone> telefone, List<TelefonesComerciais> telefoneComercial,
			List<TelefonesReferencia> telefoneReferencia, List<Parentes> parentes, List<Moradores> moradores,
			List<Veiculo> veiculos, List<Imoveis> imoveis, Infocomplementares infocomplementares, List<Emails> emailsx,
			List<Sociedades> sociedadesx) {

		this.telefone = telefone;
		this.telefoneComercial = telefoneComercial;
		this.telefoneReferencia = telefoneReferencia;
		this.parentes = parentes;
		this.moradores = moradores;
		this.veiculos = veiculos;
		this.imoveis = imoveis;
		this.infocomplementares = infocomplementares;
		this.emails = emailsx;
		this.sociedades = sociedadesx;

		// this.connection = getConnection();
	}

	public List<Telefone> getTelefone() {
		return telefone;
	}

	public void setTelefone(List<Telefone> telefone) {
		this.telefone = telefone;
	}

	public List<TelefonesComerciais> getTelefoneComercial() {
		return telefoneComercial;
	}

	public void setTelefoneComercial(List<TelefonesComerciais> telefoneComercial) {
		this.telefoneComercial = telefoneComercial;
	}

	public List<TelefonesReferencia> getTelefoneReferencia() {
		return telefoneReferencia;
	}

	public void setTelefoneReferencia(List<TelefonesReferencia> telefoneReferencia) {
		this.telefoneReferencia = telefoneReferencia;
	}

	public List<Parentes> getParentes() {
		return parentes;
	}

	public void setParentes(List<Parentes> parentes) {
		this.parentes = parentes;
	}

	public Obito getObito() {
		return obito;
	}

	public void setObito(Obito obito) {
		this.obito = obito;
	}

	public List<Moradores> getMoradores() {
		return moradores;
	}

	public void setMoradores(List<Moradores> moradores) {
		this.moradores = moradores;
	}

	public List<Vizinhos> getVizinhos() {
		return vizinhos;
	}

	public void setVizinhos(List<Vizinhos> vizinhos) {
		this.vizinhos = vizinhos;
	}

	public List<Veiculo> getVeiculos() {
		return veiculos;
	}

	public void setVeiculos(List<Veiculo> veiculos) {
		this.veiculos = veiculos;
	}

	public List<Imoveis> getImoveis() {
		return imoveis;
	}

	public void setImoveis(List<Imoveis> imoveis) {
		this.imoveis = imoveis;
	}

	public Infocomplementares getInfocomplementares() {
		return infocomplementares;
	}

	public void setInfocomplementares(Infocomplementares infocomplementares) {
		this.infocomplementares = infocomplementares;
	}

	public List<String> getOperadoras() {
		return operadoras;
	}

	public void setOperadoras(List<String> operadoras) {
		this.operadoras = operadoras;
	}

	/**
	 * @return the label6
	 */
	public String getLabel6() {
		return label6;
	}

	/**
	 * @param label6 the label6 to set
	 */
	public void setLabel6(String label6) {
		this.label6 = label6;
	}

	public String RetiraTipoLog(String Endereco) {
		String PrimeLog = Endereco.substring(0, Endereco.indexOf(" ") + 1);
		boolean IsTipoLog;

		IsTipoLog = false;

		if (PrimeLog != "") {
			if (PrimeLog.equals("RUA "))
				IsTipoLog = true;
			else if (PrimeLog.equals("R "))
				IsTipoLog = true;
			else if (PrimeLog.equals("AVENIDA "))
				IsTipoLog = true;
			else if (PrimeLog.equals("AV "))
				IsTipoLog = true;
			else if (PrimeLog.equals("PRACA "))
				IsTipoLog = true;
			else if (PrimeLog.equals("PCA "))
				IsTipoLog = true;
			else if (PrimeLog.equals("PC "))
				IsTipoLog = true;
			else if (PrimeLog.equals("TRAVESSA "))
				IsTipoLog = true;
			else if (PrimeLog.equals("TR "))
				IsTipoLog = true;
			else if (PrimeLog.equals("TV "))
				IsTipoLog = true;
			else if (PrimeLog.equals("ESTRADA "))
				IsTipoLog = true;
			else if (PrimeLog.equals("EST "))
				IsTipoLog = true;
			else if (PrimeLog.equals("PRAIA "))
				IsTipoLog = true;
			else if (PrimeLog.equals("PR "))
				IsTipoLog = true;
		}

		if (IsTipoLog == true)
			Endereco = Endereco.substring(Endereco.indexOf(" ") + 1);

		return Endereco;

	}

	public Boolean pesquisa_vizinhos(LoginMBean mb, Integer comando, Integer indice) throws SQLException {

		/*
		 * Método de pesquisa vizinhos
		 *
		 *
		 * By SMarcio em 08/10/2013
		 */

		StringBuilder sql = new StringBuilder();
		SqlToBind resultado = new SqlToBind();
		Boolean ok = false;
		Integer paginaFinal = 0;
		Integer paginaInicial = 0;
		Integer qtdpesq = 0;
		Integer pagina = 0;
		mb.setResposta_consulta(true);
		mb.setResposta_conArmazenada(false);
		mb.setResposta_endereco(false);
		mb.setResposta_nome(false);
		mb.setResposta_razao(false);
		mb.setResposta_cep(false);
		mb.setResposta_veiculo(false);
		mb.setResposta_historico_credito(false);
		mb.getPessoaSite().setTelefone(this.getTelefone().get(indice).getNumeroTelefone());
		orderTelefonesByTelefone(this.getTelefone().get(indice).getNumeroTelefone());
		indice = 0;

		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {

			Connection connection = this.getConnection();
			qtdpesq = Integer.parseInt(mb.getQtdpesq());
			pagina = mb.getPaginaVizinho();

			if (qtdpesq < 1) {

				qtdpesq = 1;

			}
			if (qtdpesq > 100) {

				qtdpesq = 100;
			}
			/* Avança registro */

			/*
			 * Se o comando 0 for passado, sempre vai iniciar a pesquisa do inicio
			 */

			if (comando == 1) {

				pagina = pagina + 1;

			}

			/* Volta registro */
			if (comando == 2) {

				pagina = pagina - 1;
			}

			paginaFinal = qtdpesq * pagina;
			paginaInicial = (paginaFinal - qtdpesq) + 1;

			if (pagina == 1 || pagina < 1) {

				paginaFinal = qtdpesq;
				paginaInicial = 1;
				pagina = 1;
			}

			/* 250 e hardcode e o maximo de registros que pesquisamos */
			if (pagina > (250 / qtdpesq)) {

				pagina = 250 / qtdpesq;
				paginaFinal = 250;
				paginaInicial = (paginaFinal - qtdpesq) + 1;
			}

			if (comando == 0) {

				pagina = 1;
				paginaFinal = qtdpesq;
				paginaInicial = 1;

			}
			paginaFinal += 1;

			mb.setPaginaVizinho(pagina);

			sql.append(" SELECT * FROM ( ");
			sql.append(" SELECT PAGINA.*,ROWNUM PAGINA_RN FROM ( ");
			sql.append(" SELECT * FROM ( ");

			if (mb.getF_vizinho() == true) {

				sql.append(
						" SELECT (SELECT STATUS FROM TELEFONES_STATUS WHERE TELEFONE = T.TELEFONE AND ROWNUM <= 1) AS STATUS_LINHA,");
				sql.append(
						" (CASE WHEN (SELECT FLAG FROM TBL_WHATSAPP T2 WHERE T2.TELEFONE = T.TELEFONE AND ROWNUM <= 1) = 1 THEN 1 ELSE 0 END) AS WHATSAPP, ");
				sql.append(
						" (SELECT IS_DATE_CONFIRME(DT_OBITO) FROM OBITO WHERE NU_CPF = CPFCGC AND ROWNUM<=1 ) AS OBITO,");
				sql.append(
						" (SELECT DT_ABERTURA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS DT_ABERTURA,");
				sql.append(" (SELECT SITUACAO FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS SITUACAO,");
				sql.append(" (SELECT FANTASIA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS FANTASIA,");
				sql.append(
						" (SELECT DESC_NATUREZA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS NATUREZA,");
				sql.append(
						" I.NOME,T.PROPRIETARIO AS PROPRIETARIO,T.ATUAL,T.TELEFONE,T.ENDERECO AS ENDERECO,T.NUMERO AS NUMERO,T.COMPLEMENTO,T.BAIRRO,T.CEP,T.CIDADE,T.UF,T.CPFCGC AS CPFCNPJ,");
				sql.append(
						" i.CPF_CONJUGE AS CPF_CONJUGE, PEGA_NOME_INFO_COMP(I.CPF_CONJUGE) AS NOME_CONJUGE, I.NOME_MAE AS MAE,TO_CHAR(I.DATA_NASC,'DD/MM/YYYY') AS NASC,' ' AS TITULO,T.OPERADORA,TO_CHAR(T.DT_INSTALACAO,'YYYY-MM-DD') AS DT_INSTALACAO, I.SIGNO AS SIGNO ");
				sql.append(
						" FROM TELEFONES T, INFO_COMPLEMENTARES I WHERE T.PROPRIETARIO IS NOT NULL AND T.CPFCGC = I.CPFCNPJ(+) ");
				sql.append(" AND T.CEP= ?  AND T.NUMERO=? AND T.CPFCGC<> ? AND ROWNUM <= ? ");

				resultado.addString(this.getTelefone().get(indice).getCep());
				resultado.addString(this.getTelefone().get(indice).getNumero());
				resultado.addString(mb.getPessoaSite().getCpfcnpj());
				resultado.addString(String.valueOf(br.com.confirmeonline.util.SQLConstantes.QTD_MAX_PESQUISA));

				if (mb.getF_vizinho_direita() == true) {

					sql.append("UNION ALL ");
				}

			}
			if (mb.getF_vizinho_direita() == true) {
				sql.append(
						"SELECT (SELECT STATUS FROM TELEFONES_STATUS WHERE TELEFONE = T.TELEFONE AND ROWNUM <= 1) AS STATUS_LINHA,");
				sql.append(
						" (CASE WHEN (SELECT FLAG FROM TBL_WHATSAPP T2 WHERE T2.TELEFONE = T.TELEFONE AND ROWNUM <= 1) = 1 THEN 1 ELSE 0 END) AS WHATSAPP, ");
				sql.append(
						"(SELECT IS_DATE_CONFIRME(DT_OBITO) FROM OBITO WHERE NU_CPF = CPFCGC AND ROWNUM<=1 ) AS OBITO,");
				sql.append(
						"(SELECT DT_ABERTURA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS DT_ABERTURA,");
				sql.append("(SELECT SITUACAO FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS SITUACAO,");
				sql.append("(SELECT FANTASIA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS FANTASIA,");
				sql.append(
						"(SELECT DESC_NATUREZA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS NATUREZA,");
				sql.append(
						"I.NOME,T.PROPRIETARIO AS PROPRIETARIO,T.ATUAL,T.TELEFONE,T.ENDERECO AS ENDERECO,T.NUMERO AS NUMERO,T.COMPLEMENTO,T.BAIRRO,T.CEP,T.CIDADE,T.UF,T.CPFCGC AS CPFCNPJ,");
				sql.append(
						"i.CPF_CONJUGE AS CPF_CONJUGE,PEGA_NOME_INFO_COMP(I.CPF_CONJUGE) AS NOME_CONJUGE,I.NOME_MAE AS MAE,TO_CHAR(I.DATA_NASC,'DD/MM/YYYY') AS NASC,' ' AS TITULO,T.OPERADORA,TO_CHAR(T.DT_INSTALACAO,'YYYY-MM-DD') AS DT_INSTALACAO, I.SIGNO AS SIGNO ");
				sql.append(
						"FROM TELEFONES T, INFO_COMPLEMENTARES I WHERE T.PROPRIETARIO IS NOT NULL AND T.CPFCGC = I.CPFCNPJ(+) ");
				sql.append("AND T.CEP= ? AND T.NUMERO >=( ? - 30) AND T.NUMERO <  ? " + "AND T.CPFCGC<> ? "
						+ "AND ( ROWNUM <= ? ) ");

				resultado.addString(this.getTelefone().get(indice).getCep());
				resultado.addString(this.getTelefone().get(indice).getNumero());
				resultado.addString(this.getTelefone().get(indice).getNumero());
				resultado.addString(mb.getPessoaSite().getCpfcnpj());
				resultado.addString(String.valueOf(br.com.confirmeonline.util.SQLConstantes.QTD_MAX_PESQUISA));

				if (mb.getF_vizinho_esquerda() == true) {

					sql.append("UNION ALL ");

				}
			}

			if (mb.getF_vizinho_esquerda() == true) {

				sql.append(
						"SELECT (SELECT STATUS FROM TELEFONES_STATUS WHERE TELEFONE = T.TELEFONE AND ROWNUM <= 1) AS STATUS_LINHA,");
				sql.append(
						" (CASE WHEN (SELECT FLAG FROM TBL_WHATSAPP T2 WHERE T2.TELEFONE = T.TELEFONE AND ROWNUM <= 1) = 1 THEN 1 ELSE 0 END) AS WHATSAPP, ");
				sql.append(
						"(SELECT TO_CHAR(TO_DATE(DT_OBITO,'YYYYMMDD'),'DD/MM/YYYY') FROM OBITO WHERE NU_CPF = CPFCGC AND ROWNUM<=1 ) AS OBITO,");
				sql.append(
						"(SELECT DT_ABERTURA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS DT_ABERTURA,");
				sql.append("(SELECT SITUACAO FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS SITUACAO,");
				sql.append("(SELECT FANTASIA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS FANTASIA,");
				sql.append(
						"(SELECT DESC_NATUREZA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS NATUREZA,");
				sql.append(
						"I.NOME,T.PROPRIETARIO AS PROPRIETARIO,T.ATUAL,T.TELEFONE,T.ENDERECO AS ENDERECO,T.NUMERO AS NUMERO,T.COMPLEMENTO,T.BAIRRO,T.CEP,T.CIDADE,T.UF,T.CPFCGC AS CPFCNPJ,");
				sql.append(
						"i.CPF_CONJUGE AS CPF_CONJUGE,PEGA_NOME_INFO_COMP(I.CPF_CONJUGE) AS NOME_CONJUGE,I.NOME_MAE AS MAE,TO_CHAR(I.DATA_NASC,'DD/MM/YYYY') AS NASC,' ' AS TITULO,T.OPERADORA,TO_CHAR(T.DT_INSTALACAO,'YYYY-MM-DD') AS DT_INSTALACAO, I.SIGNO AS SIGNO ");
				sql.append(
						"FROM TELEFONES T, INFO_COMPLEMENTARES I WHERE T.PROPRIETARIO IS NOT NULL AND T.CPFCGC = I.CPFCNPJ(+) ");
				sql.append("AND T.CEP= ? AND T.NUMERO <=( ? + 30) AND T.NUMERO > ? "
						+ "AND T.CPFCGC<>  ? AND ( ROWNUM <= ? ) ");

				resultado.addString(this.getTelefone().get(indice).getCep());
				resultado.addString(this.getTelefone().get(indice).getNumero());
				resultado.addString(this.getTelefone().get(indice).getNumero());
				resultado.addString(mb.getPessoaSite().getCpfcnpj());
				resultado.addString(String.valueOf(br.com.confirmeonline.util.SQLConstantes.QTD_MAX_PESQUISA));

			}

			sql.append(" )  ) PAGINA ) WHERE ( PAGINA_RN >= ? AND PAGINA_RN <= ?) ");
			sql.append(" ORDER BY CPFCNPJ");

			resultado.addString(String.valueOf(paginaInicial));
			resultado.addString(String.valueOf(paginaFinal));

			resultado.setSql(sql.toString());

			ok = processaConsultaVizinhos(resultado, 1, mb, paginaInicial, paginaFinal);

			/*
			 * trocou o vizinho, então tem que trocar os moradores tb, caso esteja marcada
			 * esta pesquisa
			 */

			if (mb.getF_moradores() == true) {

				Integer regra = 1;

				if (this.getTelefone().get(indice).getComplemento().length() > 0) {

					if (this.getTelefone().get(indice).getComplemento().substring(0, 1).equals("B")) {

						regra = 1;

					} else {

						regra = 2;
					}

				}
				sql.delete(0, sql.length());
				sql.append(" SELECT * FROM ( ");
				sql.append(" SELECT PAGINA.*,ROWNUM PAGINA_RN FROM ( ");
				sql.append(" SELECT * FROM ( ");
				sql.append(
						" SELECT (SELECT STATUS FROM TELEFONES_STATUS WHERE TELEFONE = T.TELEFONE AND ROWNUM <= 1) AS STATUS_LINHA,");
				sql.append(
						" (CASE WHEN (SELECT FLAG FROM TBL_WHATSAPP T2 WHERE T2.TELEFONE = T.TELEFONE AND ROWNUM <= 1) = 1 THEN 1 ELSE 0 END) AS WHATSAPP, ");
				sql.append(
						" (SELECT IS_DATE_CONFIRME(DT_OBITO) FROM OBITO WHERE NU_CPF = CPFCGC AND ROWNUM<=1 ) AS OBITO,");
				sql.append(
						" (SELECT DT_ABERTURA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS DT_ABERTURA,");
				sql.append(" (SELECT SITUACAO FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS SITUACAO,");
				sql.append(" (SELECT FANTASIA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS FANTASIA,");
				sql.append(
						" (SELECT DESC_NATUREZA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS NATUREZA,");
				sql.append(
						" I.NOME,T.PROPRIETARIO AS PROPRIETARIO,T.ATUAL,T.TELEFONE,T.ENDERECO AS ENDERECO,T.NUMERO AS NUMERO,T.COMPLEMENTO,T.BAIRRO,T.CEP,T.CIDADE,T.UF,T.CPFCGC AS CPFCNPJ,");
				sql.append(
						" i.CPF_CONJUGE AS CPF_CONJUGE,PEGA_NOME_INFO_COMP(I.CPF_CONJUGE) AS NOME_CONJUGE,I.NOME_MAE AS MAE,TO_CHAR(I.DATA_NASC,'DD/MM/YYYY') AS NASC,' ' AS TITULO,T.OPERADORA,TO_CHAR(T.DT_INSTALACAO,'YYYY-MM-DD') AS DT_INSTALACAO, I.SIGNO AS SIGNO ");
				sql.append(" FROM TELEFONES T,INFO_COMPLEMENTARES I,FINAN.CRED_MEGA_CEP M ");
				sql.append(" WHERE T.CEP=M.CEP(+) AND T.CPFCGC=I.CPFCNPJ(+) ");
				sql.append(" AND T.CEP= ? AND T.NUMERO= ? " + "AND T.CPFCGC<> ? AND ROWNUM<=250");
				sql.append(" )  ) PAGINA  WHERE ( ROWNUM <= ? ) ) WHERE ( PAGINA_RN >= ? " + "AND PAGINA_RN <= ? ) ");

				resultado.limpaLista();
				resultado.addString(this.getTelefone().get(indice).getCep());
				resultado.addString(this.getTelefone().get(indice).getNumero());
				resultado.addString(mb.getPessoaSite().getCpfcnpj());
				resultado.addString(String.valueOf(br.com.confirmeonline.util.SQLConstantes.QTD_MAX_PESQUISA));
				resultado.addString(String.valueOf(paginaInicial));
				resultado.addString(String.valueOf(paginaFinal));

				resultado.setSql(sql.toString());

				// sql.append(" ORDER BY CPFCNPJ,NUMERO, COMPLEMENTO,
				// PROPRIETARIO");

				// sql.append(" ) ORDER BY NUMERO, COMPLEMENTO, PROPRIETARIO )
				// PAGINA WHERE ( ROWNUM <=
				// "+Util.SQLConstantes.QTD_MAX_PESQUISA+" ) ) WHERE ( PAGINA_RN
				// >= '"+ paginaInicial + "' AND PAGINA_RN <= '"+ paginaFinal +
				// "' ) ");
				// sql.append(" ORDER BY CPFCNPJ,NUMERO, COMPLEMENTO,
				// PROPRIETARIO");

				ok = processaConsultaMoradores(resultado, 1, mb, regra, this.getTelefone().get(0), paginaInicial,
						paginaFinal);

			}

			return ok;

		} catch (Exception e) {
			logger.error("Erro no metodo pesquisa_vizinhos na classe Resposta:  " + e.getMessage());
			return false;
		} finally {
			if (rs != null && !rs.isClosed())
				rs.close();
			if (stmt != null && !stmt.isClosed())
				stmt.close();
		}
	}

	@SuppressWarnings("static-access")
	public Boolean pesquisa_padrao(LoginMBean mb, Integer tpconsulta, Integer comando)
			throws ServiceException, RemoteException, SQLException {

		/*
		 * Método de pesquisa padrão
		 *
		 * Este método quando acionado faz todas as pesquisas baseado nos filtros
		 * selecionados By SMarcio em 30/09/2013
		 */

		SqlToBind sql = new SqlToBind();
		SqlToBind sql2 = new SqlToBind();
		SqlToBind sql3 = new SqlToBind();
		SqlToBind sql4 = new SqlToBind();
		StringBuilder sql5 = new StringBuilder();
		// String[] armazenados = new String[5];
		Boolean ok = false;
		Boolean oks = false;
		Boolean oko = false;
		Boolean okv = false;
		Boolean okp = false;
		Boolean okm = false;
		Boolean okf = false;
		Boolean okb = false;
		Boolean okem = false;
		Boolean oktc = false;
		Boolean okveiculos = false;
		Boolean oktelref = false;
		Boolean okimovel = false;
		mb.setResposta_consulta(true);
		mb.setResposta_conArmazenada(false);
		mb.setResposta_endereco(false);
		mb.setResposta_operadora(false);
		mb.setResposta_nome(false);
		mb.setResposta_razao(false);
		mb.setResposta_cep(false);
		mb.setResposta_veiculo(false);
		mb.setResposta_historico_credito(false);
		mb.setResposta_obitoNacional(false);
		Integer posix = 0;

		this.setTabConArmazenada(this.getTabConArmazenada() + 1);
		if (this.getTabConArmazenada() > 5) {

			this.setTabConArmazenada(1);

		}
		armazenados = this.getCpfcnpjArmazenado();
		try {

			posix = this.getTabConArmazenada() - 1;
			if (posix < 0) {

				posix = 0;

			}

		} catch (Exception e) {
			logger.error("Erro na clase resposta L - 1433");
			posix = 0;

		}

		if (tpconsulta == 1) {

			armazenados[posix] = mb.getPessoaSite().getCpfcnpj();

		}

		if (tpconsulta == 2) {

			armazenados[posix] = mb.getPessoaSite().getTelefone();

		}

		String respcomercial;
		this.exibirMensagem = false;
		Boolean achouAlgo = false;
		Conexao objConexao = new Conexao();
		boolean protectedCpfcgc = false;

		protecaoDados = false;
		menorDeIdade = false;

		// Rodrigo Almeida - 17/08/2017
		String cnpjMatriz = "";

		try {
			limpaPesquisaAnterior(mb);
			List<Integer> paginas = getPaginas(mb.getPagina(), comando, mb);
			Integer paginaInicial = paginas.get(0);
			Integer paginaFinal = paginas.get(1);
			Integer paginaInicial2 = paginas.get(0);
			Integer paginaFinal2 = paginas.get(1);
			mb.setPagina(paginas.get(2));
			/* Consulta por cpfcnpj */

			this.possuiConexao = true;
			if (isProtected(mb.getPessoaSite().getCpfcnpj(), mb.getUsuario().getLogin()))
				throw new ProtectedCpfCgcException();

			if (tpconsulta == 1) {

				if (mb.getPessoaSite().getCpfcnpj().length() == 14) {
					if (mb.getF_telefone_matriz()) {
						EmpresaVo empresaVo = retornaCnpjCepMatriz(mb.getPessoaSite().getCpfcnpj(), connection);

						cnpjMatriz = empresaVo.getCnpj();

						if (empresaVo.getCep() != null && empresaVo.getCnpj() != null) {
							sql = montaConsultaCnpjJoinTelefonesAndInfoComplementares(empresaVo.getCnpj(),
									mb.getPessoaSite().getUf(), empresaVo.getCep(), mb.getUsuario().getLogin(),
									paginaInicial, paginaFinal);
						} else {
							sql = montaConsultaCnpjMatrizFromQsaEmpresas(cnpjMatriz, mb.getPessoaSite().getUf(),
									mb.getUsuario().getLogin(), paginaInicial, paginaFinal);
						}
					} else {
						sql = montaConsultaCnpjJoinTelefonesAndInfoComplementares(mb.getPessoaSite().getCpfcnpj(),
								mb.getPessoaSite().getUf(), null, mb.getUsuario().getLogin(), paginaInicial,
								paginaFinal);
					}
					sql2 = montaConsultaCnpjFromTelefones(mb.getPessoaSite().getCpfcnpj(), mb.getPessoaSite().getUf(),
							mb.getUsuario().getLogin(), paginaInicial, paginaFinal);
					sql3 = montaConsultaCnpjFromQsaEmpresas(mb.getPessoaSite().getCpfcnpj(), mb.getPessoaSite().getUf(),
							mb.getUsuario().getLogin(), paginaInicial, paginaFinal);
					sql4 = montaConsultaCnpjFromInfoComplementares(mb.getPessoaSite().getCpfcnpj(),
							mb.getPessoaSite().getUf(), mb.getUsuario().getLogin(), paginaInicial, paginaFinal);
				} else {
					sql = montaConsultaCpfJoinTelefonesAndInfoComplementares(mb.getPessoaSite().getCpfcnpj(),
							mb.getPessoaSite().getUf(), mb.getUsuario().getLogin(), paginaInicial, paginaFinal);
					sql2 = montaConsultaCpfFromTelefones(mb.getPessoaSite().getCpfcnpj(), mb.getPessoaSite().getUf(),
							mb.getUsuario().getLogin(), paginaInicial, paginaFinal);
					sql3 = montaConsultaCpfFromInfoComplementares(mb.getPessoaSite().getCpfcnpj(),
							mb.getPessoaSite().getUf(), mb.getUsuario().getLogin(), paginaInicial, paginaFinal);
				}
				/*
				 * Aciona o método processaConsulta Que é responsável por ir ao banco de dados,
				 * ler as informações, e coloca-las dentro do managedBean By SMarcio em
				 * 30/09/2013
				 */
				this.setForm_active_cpfcnpj("form active");
				this.setForm_active_con_armazenada("form");
				this.setForm_active_telefone("form");
				this.setForm_active_operadora("form");
				this.setForm_active_cep("form");
				this.setForm_active_endereco("form");
				this.setForm_active_historico_credito("form");
				this.setForm_active_obitoNacional("form");
				this.setForm_active_nome("form");
				this.setForm_active_razao_social("form");
				this.setForm_active_veiculos("form");

				// Rodrigo Almeida -- 04/05/2018
				// O CNPJ não cadastrado na QSA_EMPRESAS estava sendo retornado no sql, pois o
				// mesmo tem um left join da Info Complementar
				// que só guarda dados de Pessoa Fisica com a tabela Telefones. Sendo assim, os
				// dados retornam incompletos, ou seja, somente dados
				// do Telefone e o sistema não faz acesso ao WebService da Receita para retornar
				// as informações sobre a empresa.

				// Dados PESSOA JURÍDICA
				if (mb.getPessoaSite().getCpfcnpj().length() == 14) {
					ok = processaConsulta(sql3, 1, mb);

					if (ok == false) {
						// Se não houver retorno dos dados na QSA_EMPRESA, buscar os dados no WebService
						// da Receita

						buscaInfoWSReceitaPJ(mb);
					}

				}

//                ----------------------------------------------------------------------------------------------------------------------------------

				ok = processaConsulta(sql, 1, mb);

				// Se a pesquisa for pela Filial, e a mesma retornou o CEP e o
				// CNPJ, mas não encontrou dados nas tabelas TELEFONE e
				// INFO_COMPLEMENTARES
				// consultar somente a QSA_EMPRESAS
				if (mb.getF_telefone_matriz() && ok == false) {

					sql = montaConsultaCnpjMatrizFromQsaEmpresas(cnpjMatriz, mb.getPessoaSite().getUf(),
							mb.getUsuario().getLogin(), paginaInicial, paginaFinal);
					ok = processaConsulta(sql, 1, mb);
				}

				if (!ok && menorDeIdade==false) {
					ok = processaConsulta(sql2, 1, mb);
					if (!ok) {
						mb.setF_telefone_fixos_celulares(false);
						ok = processaConsulta(sql3, 1, mb);

						if (mb.getUsuario().getCredcodigo() != null
								&& mb.getUsuario().getCredcodigo().equals(Constantes.CREDICODIGO_ATENTO)) {
							if (!ok && mb.getPessoaSite().getCpfcnpj().length() == 11) {
								ok = buscaUsuarioNaoLocalizadoMec(mb.getPessoaSite().getCpfcnpj());
							}
						}
						if (!ok && mb.getPessoaSite().getCpfcnpj().length() == 14) {
							ok = processaConsulta(sql4, 1, mb);
						}
					}

				}

				if (mb.getUsuario().getCredcodigo() != null
						&& mb.getUsuario().getCredcodigo().equals(Constantes.CREDICODIGO_ATENTO)) {
					if (mb.getPessoaSite().getCpfcnpj().length() == 11) {
						if (isNullOrEmpty(this.infocomplementares.getNome())
								|| isNullOrEmpty(this.infocomplementares.getNomemae())
								|| isNullOrEmpty(this.infocomplementares.getDtnasc())) {
							updateRegistroInfoComplementar(this.infocomplementares.getCpfcnpj());
						}
					}
				}

				if (!ok && menorDeIdade==false) {
					buscaInfoWSReceitaPJ(mb);
//                    if (mb.getPessoaSite().getCpfcnpj().length() == 14) {
//                        Empresa empresa = null;
//
//                        if (habilitaWsReceita()) {
//                            // empresa =
//                            // this.getInformacoesWsReceita(mb.getPessoaSite().getCpfcnpj());
//                            // Rodrigo Almeida - 11/12/2017
//                            empresa = new ConsultaReceitaService()
//                                    .listarDadosReceitaByCNPJ(mb.getPessoaSite().getCpfcnpj());
//                        }
//
//                        if (!isNullOrEmpty(empresa)) {
//                            this.setLabel1("Dt. Fundação:");
//                            this.infocomplementares
//                                    .setDtnasc(empresa.getDataAbertura() == null ? "" : empresa.getDataAbertura());
//
//                            this.infocomplementares
//                                    .setSigno(empresa.getNomeFantasia() == null ? "" : empresa.getNomeFantasia());
//                            this.setLabel2("Nome Fantasia:");
//
//                            this.infocomplementares.setSexo(
//                                    empresa.getNaturezaJuridica() == null ? "" : empresa.getNaturezaJuridica());
//                            this.setLabel3("Natureza:");
//
//                            this.infocomplementares.setNomemae(
//                                    empresa.getSituacaoCadastral() == null ? "" : empresa.getSituacaoCadastral());
//                            this.setLabel4("Situação:");
//                            this.setLabel5("");
//                            this.infocomplementares
//                                    .setRamoAtvi(empresa.getCnaeprincipal() == null ? "" : empresa.getCnaeprincipal());
//
//                            new PessoaJuridicaDAO(getConnection()).registraRetornoQSAEmpresa(empresa);
//
//                            ok = true;
//                        } else {
//                            ok = false;
//                        }
//                    }
					this.infocomplementares.setCpfcnpj(mb.getPessoaSite().getCpfcnpj());
					mb.getPessoaSite().setCpfcnpj(mb.getPessoaSite().getCpfcnpj());

				}

				String[] nomeservidor = mb.getServidor();
				if (ok) {
					objConexao.registraConsulta(this.getConnection(), "CO-CPFCNPJ", mb.getPessoaSite().getCpfcnpj(),
							mb.getUsuario().getLogin(), mb.getUsuario().getSenha(), "CONFI", mb.getUsuario().getIP(),
							mb.getCanonicalName());
				}

				limpaQueries(sql.getSql() != null ? new StringBuilder(sql.getSql()) : new StringBuilder(),
						sql2.getSql() != null ? new StringBuilder(sql2.getSql()) : new StringBuilder(),
						sql3.getSql() != null ? new StringBuilder(sql3.getSql()) : new StringBuilder(),
						sql4.getSql() != null ? new StringBuilder(sql4.getSql()) : new StringBuilder());

			}

			/* Consulta por telefone */

			if (tpconsulta == 2 ) {

				sql = montaConsultaTipoDois(mb.getPessoaSite().getTelefone(), mb.getUsuario().getLogin(),
						br.com.confirmeonline.util.SQLConstantes.QTD_MAX_PESQUISA, paginaInicial, paginaFinal);

				mb.getPessoaSite().setCpfcnpj("");

				ok = processaConsulta(sql, 1, mb);

//                Rodrigo Almeida 28/10/2019
//                Verificando se um determinado CPF está relacionado na tabela TB_PROTECAO_DADOS_PESSOAIS
				protecaoDados = false;
				protecaoDados = verificaProtecaoDadosPessoais(mb.getPessoaSite().getCpfcnpj(), "");

				if (protecaoDados == false) {
					this.setForm_active_cpfcnpj("form");
					this.setForm_active_telefone("form active");
					this.setForm_active_operadora("form");
					this.setForm_active_cep("form");
					this.setForm_active_endereco("form");
					this.setForm_active_historico_credito("form");
					this.setForm_active_nome("form");
					this.setForm_active_razao_social("form");
					this.setForm_active_veiculos("form");
					String[] nomeservidor = mb.getServidor();
					if (ok) {
						objConexao.registraConsulta(this.getConnection(), "CO-TELEFONE",
								mb.getPessoaSite().getTelefone(), mb.getUsuario().getLogin(),
								mb.getUsuario().getSenha(), "CONFI", mb.getUsuario().getIP(), mb.getCanonicalName());
					}
					if (ok == true) {

						mb.getPessoaSite().setCpfcnpj(this.getTelefone().get(0).getCpfcnpj());
						if (mb.getDynamoDBService().isPpe(this.getTelefone().get(0).getCpfcnpj())) {
							throw new PpeException();
						}

					}
				}
				limpaQueries(sql.getSql() != null ? new StringBuilder(sql.getSql()) : new StringBuilder(),
						sql2.getSql() != null ? new StringBuilder(sql2.getSql()) : new StringBuilder(),
						sql3.getSql() != null ? new StringBuilder(sql3.getSql()) : new StringBuilder(),
						sql4.getSql() != null ? new StringBuilder(sql4.getSql()) : new StringBuilder());

			}
			/* Consulta por operadora */

			if (tpconsulta == 3) {
				this.telefoneOperadora = Search.searchByTelefone(mb.getPessoaSite().getTelefoneOperadora());

				oko = !estaVazioOuNulo(this.telefoneOperadora.getOperadora());

				mb.setResposta_cep(false);
				mb.setResposta_consulta(false);
				mb.setResposta_conArmazenada(false);
				mb.setResposta_endereco(false);
				mb.setResposta_historico_credito(false);
				mb.setResposta_mapa(false);
				mb.setResposta_nome(false);
				mb.setResposta_razao(false);
				mb.getPessoaSite().setCpfcnpj("");
				this.setForm_active_cpfcnpj("form");
				this.setForm_active_telefone("form");
				this.setForm_active_cep("form");
				this.setForm_active_endereco("form");
				this.setForm_active_operadora("form active");
				this.setForm_active_historico_credito("form");
				this.setForm_active_nome("form");
				this.setForm_active_razao_social("form");
				this.setForm_active_veiculos("form");

				String[] nomeservidor = mb.getServidor();
				if (oko) {
					objConexao.registraConsulta(this.getConnection(), "OPERADORA", mb.getPessoaSite().getTelefone(),
							mb.getUsuario().getLogin(), mb.getUsuario().getSenha(), "CONFI", mb.getUsuario().getIP(),
							mb.getCanonicalName());
				}

				mb.setResposta_operadora(oko);

				limpaQueries(sql.getSql() != null ? new StringBuilder(sql.getSql()) : new StringBuilder(),
						sql2.getSql() != null ? new StringBuilder(sql2.getSql()) : new StringBuilder(),
						sql3.getSql() != null ? new StringBuilder(sql3.getSql()) : new StringBuilder(),
						sql4.getSql() != null ? new StringBuilder(sql4.getSql()) : new StringBuilder());

			}

			if (protecaoDados == false && menorDeIdade==false) {
				if (ok == true) {

					okv = pesquisaVizinhos(mb, comando, mb.getF_vizinho(), mb.getF_vizinho_esquerda(),
							mb.getF_vizinho_direita());

					if (mb.getF_parentes() == true) {

						okp = pesquisaParentesDM_Parentes(mb, comando);

						if (bPesquisaDM_Parente == false) {
							okp = pesquisaParentes(mb, comando);
						}
					}
					if (mb.getF_moradores() == true) {
						okm = pesquisaMoradores(mb, comando);
					}

				}
				if (mb.getF_socios() == true ) {
					oks = pesquisaSocios2(mb, comando);
				}
				if (mb.getF_enderecosComerciais() != null && mb.getF_enderecosComerciais() == true) {
					oks = pesquisaEnderecosComerciais(mb, comando);
				}
				if (mb.getF_obitos() == true) {
					if (mb.getUsuario().getObitoNacional())
						okb = pesquisaObitoCompleto(mb, comando);
					else
						okb = pesquisaObito(mb, comando);
				}
				if (mb.getF_email() == true) {
					okem = pesquisaEmail(mb, comando);
				}
				if (mb.getF_tel_comercial() == true) {
					oktc = pesquisaTelComercial(mb, comando);
				}
				if (mb.getF_veiculo() == true) {
					okveiculos = pesquisaVeiculos(mb, comando);
				}
				if (mb.getF_telefone_referencia() == true) {
					oktelref = pesquisaTelRef(mb, comando);
				}
				if (mb.getF_imoveis() == true) {
					okimovel = pesquisaImoveis(mb, comando);
				}
				if (mb.getF_filhos()) {
					// okf = pesquisaFilhos(mb, 0);
					okf = pesquisaFilhosDM_Parentes(mb, 0);
				}
				mb.setRespostatelefone(ok);
				achouAlgo = false;
				if (ok || oks || okb || okem || oktc || okveiculos || oktelref || okimovel || oko || okf) {
					achouAlgo = true;
				}
				if (!ok && !oks) {
					mb.setResposta_consulta(false);
					mb.setResposta_conArmazenada(false);
					mb.setResposta_endereco(false);
					mb.setResposta_endereco(false);
					mb.setResposta_nome(false);
					mb.setResposta_razao(false);
					mb.setResposta_cep(false);
					mb.setResposta_veiculo(false);
					mb.setResposta_historico_credito(false);
				}
			}
			this.exibirMensagem = true;

		} catch (ProtectedCpfCgcException e) {
			achouAlgo = false;
			protectedCpfcgc = true;
		} catch (PpeException pe) {
			throw pe;
		} catch (Exception e) {
			logger.error("Erro na classe Resposta L = 1811" + e.getMessage());
			achouAlgo = false;
		} finally {
			this.exibirMensagem = true;
			this.possuiConexao = false;
			releaseConnection();
		}

		// Rodrigo Almeida - O requisição abaixo não retorna, travando o
		// sistema. Por isso foi comentada
		// Data Alteração: 30/01/2018
		// if(tpconsulta==1&&!achouAlgo&&!protectedCpfcgc){
		// BuscaConfirmeOnline_PortType buscaConfirmeOnlineService = new
		// BuscaConfirmeOnline_ServiceLocator().getBuscaConfirmeOnlinePort();
		// buscaConfirmeOnlineService.insertCpfcgcNaoLoc(mb.getPessoaSite().getCpfcnpj(),
		// mb.getUsuario().getLogin(),
		// WebServiceConstants.BUSCA_CONFIRME_ONLINE_KEY);
		// }
		return achouAlgo;

	}

	private SqlToBind montaConsultaTipoDois(String telefone, String login, int qtdMaxPesquisa, Integer paginaInicial,
			Integer paginaFinal) {

		StringBuilder sql = new StringBuilder();
		SqlToBind resultado = new SqlToBind();

		sql.append("SELECT * FROM (");
		sql.append("SELECT PAGINA.*,ROWNUM PAGINA_RN FROM (");

		sql.append(
				"SELECT (SELECT STATUS FROM TELEFONES_STATUS WHERE TELEFONE = T.TELEFONE AND ROWNUM <= 1) AS STATUS_LINHA,");
		sql.append(
				" (CASE WHEN (SELECT FLAG FROM TBL_WHATSAPP T2 WHERE T2.TELEFONE = T.TELEFONE AND ROWNUM <= 1) = 1 THEN 1 ELSE 0 END) AS WHATSAPP, ");
		sql.append("(SELECT IS_DATE_CONFIRME(DT_OBITO) FROM OBITO WHERE NU_CPF = CPFCGC AND ROWNUM<=1 ) AS OBITO,");
		sql.append("(SELECT DT_ABERTURA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS DT_ABERTURA,");
		sql.append("(SELECT SITUACAO FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS SITUACAO,");
		sql.append("(SELECT FANTASIA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS FANTASIA,");
		sql.append("(SELECT DESC_NATUREZA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS NATUREZA,");
		sql.append(
				"I.NOME,T.PROPRIETARIO AS PROPRIETARIO,T.ATUAL,T.TELEFONE,T.ENDERECO AS ENDERECO,T.NUMERO AS NUMERO,T.COMPLEMENTO,T.BAIRRO,T.CEP,T.CIDADE,T.UF,T.CPFCGC AS CPFCNPJ,");
		sql.append(
				"i.CPF_CONJUGE AS CPF_CONJUGE,PEGA_NOME_INFO_COMP(I.CPF_CONJUGE) AS NOME_CONJUGE,I.NOME_MAE AS MAE,I.NOME_PAI AS PAI, TO_CHAR(I.DATA_NASC,'DD/MM/YYYY') AS NASC,' ' AS TITULO,T.OPERADORA,TO_CHAR(T.DT_INSTALACAO,'YYYY-MM-DD') AS DT_INSTALACAO, I.SIGNO AS SIGNO,T.TIPO ");
		sql.append(
				"FROM TELEFONES T, INFO_COMPLEMENTARES I WHERE T.PROPRIETARIO IS NOT NULL AND T.CPFCGC = I.CPFCNPJ(+) AND T.TELEFONE = ?");
		sql.append(" AND NOT EXISTS (SELECT * FROM PROTECAO_CPFCNPJ WHERE USUARIO = ? AND CPFCGC = T.CPFCGC ) ");

		sql.append("UNION ALL ");

		sql.append(
				"SELECT * FROM(SELECT (SELECT STATUS FROM TELEFONES_STATUS WHERE TELEFONE = T.TELEFONE AND ROWNUM <= 1) AS STATUS_LINHA,");
		sql.append(
				" (CASE WHEN (SELECT FLAG FROM TBL_WHATSAPP T2 WHERE T2.TELEFONE = T.TELEFONE AND ROWNUM <= 1) = 1 THEN 1 ELSE 0 END) AS WHATSAPP, ");
		sql.append("(SELECT IS_DATE_CONFIRME(DT_OBITO) FROM OBITO WHERE NU_CPF = CPFCGC AND ROWNUM<=1 ) AS OBITO,");
		sql.append("(SELECT DT_ABERTURA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS DT_ABERTURA,");
		sql.append("(SELECT SITUACAO FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS SITUACAO,");
		sql.append("(SELECT FANTASIA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS FANTASIA,");
		sql.append("(SELECT DESC_NATUREZA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS NATUREZA,");
		sql.append(
				"I.NOME,T.PROPRIETARIO AS PROPRIETARIO,T.ATUAL,T.TELEFONE,T.ENDERECO AS ENDERECO,T.NUMERO AS NUMERO,T.COMPLEMENTO,T.BAIRRO,T.CEP,T.CIDADE,T.UF,T.CPFCGC AS CPFCNPJ,");
		sql.append(
				"i.CPF_CONJUGE AS CPF_CONJUGE,PEGA_NOME_INFO_COMP(I.CPF_CONJUGE) AS NOME_CONJUGE,I.NOME_MAE AS MAE,I.NOME_PAI AS PAI,TO_CHAR(I.DATA_NASC,'DD/MM/YYYY') AS NASC,' ' AS TITULO,T.OPERADORA,TO_CHAR(T.DT_INSTALACAO,'YYYY-MM-DD') AS DT_INSTALACAO,  I.SIGNO AS SIGNO,T.TIPO ");
		sql.append(
				"FROM TELEFONES T, INFO_COMPLEMENTARES I WHERE T.PROPRIETARIO IS NOT NULL AND T.CPFCGC = I.CPFCNPJ(+) AND T.CPFCGC = ( SELECT Z.CPFCGC FROM TELEFONES Z WHERE Z.TELEFONE = ? AND ROWNUM <= 1) AND t.telefone <> ? ");
		sql.append(" AND NOT EXISTS (SELECT * FROM PROTECAO_CPFCNPJ WHERE USUARIO = ? AND CPFCGC = T.CPFCGC ) ");
		sql.append("AND ( ROWNUM <=  ? ) ORDER BY TO_NUMBER(ATUAL) DESC, WHATSAPP DESC) ) PAGINA  ");
		sql.append(") WHERE  ( PAGINA_RN >= ? AND  PAGINA_RN <= ? ) ");

		resultado.limpaLista();
		resultado.addString(telefone);
		resultado.addString(login);
		resultado.addString(telefone);
		resultado.addString(telefone);
		resultado.addString(login);
		resultado.addString(String.valueOf(qtdMaxPesquisa));
		resultado.addString(String.valueOf(paginaInicial));
		resultado.addString(String.valueOf(paginaFinal));

		resultado.setSql(sql.toString());

		return resultado;

	}

	/**
	 * Busca um dado não localizado no webservice da educação Atualiza o registro na
	 * tabela INFO_COMPLEMENTARES
	 *
	 * @param cpf
	 * @throws SQLException
	 */
	public boolean buscaUsuarioNaoLocalizadoMec(String cpf) throws SQLException {
		Statement statement = null;
		Pessoa pessoa = new RobotDao().findPessoaByCpfMec(cpf);
		boolean possuiParametro = false;
		try {
			if (pessoa != null) {
				if (isNullOrEmpty(pessoa.getNomeMae()) && !pessoa.getNomeMae().equals("-1")) {
					this.infocomplementares.setNomemae(pessoa.getNomeMae());
					possuiParametro = true;
				}
				if (isNullOrEmpty(pessoa.getDataNascimento()) && !pessoa.getDataNascimento().equals("-1")) {
					this.infocomplementares.setDtnasc(pessoa.getDataNascimento());
					possuiParametro = true;
				}
				if (isNullOrEmpty(pessoa.getNome()) && !pessoa.getNome().equals("-1")) {
					this.infocomplementares.setNomemae(pessoa.getNome());
					possuiParametro = true;
				}

				if (possuiParametro) {
					StringBuilder query = new StringBuilder();
					query.append("INSERT INTO INFO_COMPLEMENTARES VALUES (NOME, NOME_MAE, DATA_NASC) VALUES ("
							+ pessoa.getNome() + "," + pessoa.getNomeMae() + "," + pessoa.getDataNascimento() + ")");
					statement = connection.createStatement();
					statement.execute(query.toString());
					logger.error(
							"O Registro com o CPF " + pessoa.getCpf() + " foi inserido na INFO_COMPLEMENTAR (ATENTO)");

					return true;
				} else {
					return false;
				}

			} else {
				return false;
			}
		} catch (Exception e) {
			logger.error("Houve um erro ao atualizar um registro na tabela INFO_COMPLEMENTAR para o CPF"
					+ pessoa.getCpf() + "(ATENTO) - " + e.getMessage());
			return false;
		} finally {
			if (statement != null) {
				statement.close();
			}
		}
	}

	/**
	 * Atualiza os valores que serão exibidos na tela para os dados pessoais
	 * Atualiza o registro na tabela INFO_COMPLEMENTARES
	 *
	 * @param cpf
	 * @throws SQLException
	 */
	public void updateRegistroInfoComplementar(String cpf) throws SQLException {
		Statement statement = null;
		Pessoa pessoa = new RobotDao().findPessoaByCpfMec(cpf);
		Boolean existeParametro = false;
		try {

			if (pessoa != null) {

				if (isNullOrEmpty(this.infocomplementares.getNomemae())) {
					this.infocomplementares.setNomemae(pessoa.getNomeMae());
				}

				if (isNullOrEmpty(this.infocomplementares.getDtnasc())) {
					this.infocomplementares.setDtnasc(pessoa.getDataNascimento());
				}

				if (isNullOrEmpty(this.infocomplementares.getNome())) {
					this.infocomplementares.setNomemae(pessoa.getNome());
				}

				StringBuilder query = new StringBuilder();

				int i = 0;

				query.append("UPDATE INFO_COMPLEMENTARES SET ");

				if (isNullOrEmpty(this.infocomplementares.getNomemae()) && !pessoa.getNomeMae().equals("")) {
					if (i > 0) {
						query.append(",");
					}
					query.append("NOME_MAE = '" + pessoa.getNomeMae() + "' ");
					i++;
					existeParametro = true;
				}

				if (isNullOrEmpty(this.infocomplementares.getDtnasc())) {
					if (i > 0) {
						query.append(",");
					}
					query.append("DATA_NASC = TO_DATE('" + pessoa.getDataNascimento() + "','DD/MM/YYYY') ");
					i++;
					existeParametro = true;
				}

				if (isNullOrEmpty(this.infocomplementares.getNome())) {
					if (i >= 0 && i <= 3) {
						query.append(",");
					}
					query.append("NOME = '" + pessoa.getNome() + "' ");
					i++;
					existeParametro = true;
				}

				/** APENAS EXECUTA A INSERÇÃO CASO A QUERY POSSUA PARAMETROS **/
				if (existeParametro) {
					query.append("WHERE CPFCNPJ = '" + pessoa.getCpf() + "' ");
					statement = connection.createStatement();
					statement.execute(query.toString());
					logger.error("O Registro com o CPF " + pessoa.getCpf()
							+ " foi atualizado na INFO_COMPLEMENTAR (ATENTO)");
				}

			}
		} catch (Exception e) {
			logger.error("Houve um erro ao atualizar um registro na tabela INFO_COMPLEMENTAR para o CPF"
					+ pessoa.getCpf() + "(ATENTO) - " + e.getMessage());
		} finally {
			if (statement != null) {
				statement.close();
			}
		}
	}

	public String GET_RAMO_MD_NOVO(String RA, LoginMBean mx) throws SQLException {
		java.sql.Statement stmtN = null;
		ResultSet rs = null;
		String sql = "", sql2 = "";
		String subclasse = "";
		String retorno = "";

		try {
			if (!estaVazioOuNulo(RA)) {
				subclasse = RA.substring(0, 7);
				sql = "SELECT GRUPO_DESC FROM TB_CNAE WHERE SUBCLASSE = '" + subclasse + "' AND ROWNUM <=1";
				stmtN = this.getConnection().createStatement();
				rs = stmtN.executeQuery(sql);
				if (rs != null && rs.next()) {
					retorno = this.Filtra(rs.getString("GRUPO_DESC") == null ? "" : rs.getString("GRUPO_DESC"));
				}

			}
		} catch (Exception e) {
			logger.error("Erro no metodo GET_RAMO_MD_NOVO da classe resposta " + e.getMessage());
			retorno = "";
		} finally {
			if (rs != null && !rs.isClosed())
				rs.close();
			if (stmtN != null && !stmtN.isClosed())
				stmtN.close();
		}

		return retorno;

	}

	public Boolean processaConsulta(SqlToBind consulta, int tipopesquisa, LoginMBean mx) throws SQLException {
		java.sql.Statement stmtN = null;
		ResultSet rs = null;
		int ind = 0;
		String nome = "";
		String sexo = "";
		String cpfcnpj = "";
		Boolean achou = false;
		Connection conn = this.getConnection();
		try {
			stmtN = conn.prepareStatement(consulta.getSql());

			for (int i = 0; i <= consulta.getBinds().size() - 1; i++)
				((PreparedStatement) stmtN).setString(i + 1, consulta.getBinds().get(i));

			rs = ((PreparedStatement) stmtN).executeQuery();
			ind = 0;
			int id = 1;
			String nasc = "";
			String emails = "";
			Telefone t;
			String dtobito = "";
			this.infocomplementares = new Infocomplementares();
			List<String> infosQsaEmpresas;
			if (tipopesquisa == 1) {

				this.telefone = new ArrayList<Telefone>();

			}
			
			
			
			mensagem="";

			while (rs != null && rs.next() && menorDeIdade==false) {
				cpfcnpj = this.Filtra(rs.getString("CPFCNPJ") == null ? "" : rs.getString("CPFCNPJ"));
				String birthday = dao.findBirthday(cpfcnpj);
				Integer idade = Utils.calculaIdade(birthday);
				
				if (idade == null) {
					idade=18;
				}
				
				
				if (idade < 18 && cpfcnpj.length() == 11) {
//					System.out.println("Menor de idade");
					mensagem="Menor de Idade";
					menorDeIdade=true;
				}else {

				t = new Telefone();

				String telefone = null;
				try {

					telefone = this.Filtra(rs.getString("TELEFONE"));
				} catch (Exception ignore) {

					telefone = "";

				}
				/*
				 * tipos de pesquisa 1 - pesquisa por telefone 2 - parentes
				 *
				 */

				/* Pesquisa CpfCnpj ConfirmeOnLine New */

				cpfcnpj = this.Filtra(rs.getString("CPFCNPJ") == null ? "" : rs.getString("CPFCNPJ"));
				if (tipopesquisa == 1) {

					if (ind == 0) {

						nome = this.Filtra(rs.getString("NOME") == null ? "" : rs.getString("NOME"));

						if (nome.equals("")) {

							nome = this
									.Filtra(rs.getString("PROPRIETARIO") == null ? "" : rs.getString("PROPRIETARIO"));

						}

						this.infocomplementares.setNome(nome);
//						String birthday = dao.findBirthday(cpfcnpj);
//						Integer idade = Utils.calculaIdade(birthday);
						this.infocomplementares
								.setDtnasc(rs.getString("NASC") == null ? "" : rs.getString("NASC") + idade);
						this.infocomplementares
								.setDtnasc(birthday != null ? birthday + " - " + idade + " anos." : null);

						sexo = this.Filtra(GetSexo(nome, cpfcnpj));
						if (sexo.equals("M")) {

							sexo = "MASCULINO";

						}
						if (sexo.equals("F")) {

							sexo = "FEMININO";

						}

						if (cpfcnpj.length() < 14) {
							this.setLabel1("Dt. Nascimento:");

							this.infocomplementares
									.setSigno(Utils.findSigno(this.infocomplementares.getDtnasc(), "dd/MM/yyyy"));
							this.setLabel2("Signo:");

							this.infocomplementares.setSexo(sexo);
							this.setLabel3("Sexo:");

							this.infocomplementares
									.setNomemae(this.Filtra(rs.getString("MAE") == null ? "" : rs.getString("MAE")));
							this.setLabel4("Nome da Mãe:");

							this.infocomplementares
									.setNomePai(this.Filtra(rs.getString("PAI") == null ? "" : rs.getString("PAI")));
							if (!isNullOrEmpty(this.infocomplementares.getNomePai())) {
								this.setLabel6("Nome do Pai:");
							}

							this.infocomplementares.setNomeConjuge(this
									.Filtra(rs.getString("NOME_CONJUGE") == null ? "" : rs.getString("NOME_CONJUGE")));

							this.infocomplementares.setCpfConjuge(this
									.Filtra(rs.getString("CPF_CONJUGE") == null ? "" : rs.getString("CPF_CONJUGE")));

							this.infocomplementares
									.setTituloEleitor(this.getTituloEleitoral(cpfcnpj, this.getConnection()));
							this.setLabel5("Título de Eleitor:");

							this.setLabel6("Grau de Parentesco: yy");

							dtobito = this.Filtra(rs.getString("OBITO") == null ? "" : rs.getString("OBITO"));
							dtobito = ObitoDao.findDataObito(mx, cpfcnpj, this.infocomplementares.getNome(), conn);

							this.infocomplementares.setDtobito(dtobito);

						}

						if (cpfcnpj.length() == 14) {
							infosQsaEmpresas = getQsaEmpresasInfo(cpfcnpj);

							this.setLabel1("Dt. Fundação:");

							this.infocomplementares
									.setSigno(infosQsaEmpresas.get(2) == null ? "" : infosQsaEmpresas.get(2));
							this.setLabel2("Nome Fantasia:");

							this.infocomplementares
									.setSexo(infosQsaEmpresas.get(3) == null ? "" : infosQsaEmpresas.get(3));
							this.setLabel3("Natureza:");

							this.infocomplementares
									.setNomemae(infosQsaEmpresas.get(1) == null ? "" : infosQsaEmpresas.get(1));
							this.setLabel4("Situação:");
							this.setLabel5("");
							this.infocomplementares
									.setRamoAtvi(infosQsaEmpresas.get(4) == null ? "" : infosQsaEmpresas.get(4));
							// this.infocomplementares.setNome(isNullOrEmpty(infosQsaEmpresas.get(5))
							// ? nome : infosQsaEmpresas.get(5));
						}
						this.infocomplementares.setCpfcnpj(cpfcnpj);
						mx.getPessoaSite().setCpfcnpj(cpfcnpj);

						t.setEoprimeiro(true);
						// Verifica se o nome do telefone é diferente do nome do
						// cpf

					}

					verificaNomesDivergentes(this.infocomplementares, t.getInfotelefone(), cpfcnpj, telefone, conn);

					t.setId(Integer
							.parseInt(this.Filtra(rs.getString("PAGINA_RN") == null ? "" : rs.getString("PAGINA_RN"))));
					t.setProprietario(
							this.Filtra(rs.getString("PROPRIETARIO") == null ? "" : rs.getString("PROPRIETARIO")));
					t.setUf(this.Filtra(rs.getString("UF") == null ? "" : rs.getString("UF")));
					t.setCpfcnpj(cpfcnpj);
					t.setNumeroTelefone(
							this.Filtra(rs.getString("TELEFONE") == null ? "-----------" : rs.getString("TELEFONE")));

					/*
					 * Verifica se o cliente possui direito a base que ele esta consultando, se não
					 * mostra alguns dados mas os endereços e telefones não mostra
					 */

					if (Conexao.GetSerial(conn, mx.getUsuario().getLogin()).indexOf(t.getUf()) != -1) {

						t.setNumeroTelefone(this
								.Filtra(rs.getString("TELEFONE") == null ? "-----------" : rs.getString("TELEFONE")));
						t.setEndereco(this.Filtra(rs.getString("ENDERECO") == null ? "" : rs.getString("ENDERECO")));
						t.setNumero(this.Filtra(rs.getString("NUMERO") == null ? "" : rs.getString("NUMERO")));
						t.setComplemento(
								this.Filtra(rs.getString("COMPLEMENTO") == null ? "" : rs.getString("COMPLEMENTO")));
						t.setBairro(this.Filtra(rs.getString("BAIRRO") == null ? "" : rs.getString("BAIRRO")));
						t.setCidade(this.Filtra(rs.getString("CIDADE") == null ? "" : rs.getString("CIDADE")));
						t.setCep(this.Filtra(rs.getString("CEP") == null ? "" : rs.getString("CEP")));

						if ((mx.getF_telefone_fixos_celulares() != null) && (mx.getF_telefone_fixos_celulares())) {
							t.setTipo(rs.getString("TIPO"));
						}
						t.setProcon(this.proconSP(t.getNumeroTelefone(), conn));
						if (t.getNumeroTelefone().length() < 9) {

							t.setNumeroTelefone("-----------");

						}

						/*
						 * coloca o endereço da info_complementares para aparecer pelo menos ja que nao
						 * tem nenhum endereco
						 */

						if (t.getNumeroTelefone() == "-----------") {

							try {

								String[] reginfo = dao.findEndercoInfo(cpfcnpj);
								reginfo[0] = this.Filtra(reginfo[0] == null ? "" : reginfo[0]);
								reginfo[1] = this.Filtra(reginfo[1] == null ? "" : reginfo[1]);
								reginfo[2] = this.Filtra(reginfo[2] == null ? "" : reginfo[2]);
								reginfo[3] = this.Filtra(reginfo[3] == null ? "" : reginfo[3]);
								reginfo[4] = this.Filtra(reginfo[4] == null ? "" : reginfo[4]);
								reginfo[5] = this.Filtra(reginfo[5] == null ? "" : reginfo[5]);
								reginfo[6] = this.Filtra(reginfo[6] == null ? "" : reginfo[6]);

								t.setEndereco(reginfo[0]);
								t.setNumero(reginfo[1]);
								t.setComplemento(reginfo[2]);
								t.setCidade(reginfo[3]);
								t.setUf(reginfo[4]);
								t.setCep(reginfo[5]);
								t.setBairro(reginfo[6]);

							} catch (Exception e) {

								t.setNumeroTelefone("-----------");
								t.setEndereco("-");
								t.setNumero("-");
								t.setComplemento("-");
								t.setBairro("-");
								t.setCidade("-");
								t.setCep("-");
								t.setProcon("-");
								logger.error("Erro no metodo  processaConsultaVizinhos da classe Respost: "
										+ e.getMessage());

							}

						}

					} else {

						t.setNumeroTelefone("--Estado não contratado.");
						t.setEndereco("Estado não contratado ");
						t.setNumero("-");
						t.setComplemento("-");
						t.setBairro("-");
						t.setCidade("-");
						t.setCep("-");
						t.setProcon("-");

					}
					t.setStatusLinha(
							this.Filtra(rs.getString("STATUS_LINHA") == null ? "" : rs.getString("STATUS_LINHA")));
					t.setAtual(this.Filtra(rs.getString("ATUAL") == null ? "" : rs.getString("ATUAL")));

					t.setRatingTelefone(t.getAtual(), getUseStatus());
					t.setWhatsApp(rs.getString("WHATSAPP"));
					this.telefone.add(t);
					ind++;
					id++;

				}

			}

			}
			this.paginadadosbasicosAnt = false;
			this.paginadadosbasicosProx = false;
			if (!(mx.getPagina().equals(1)) && this.telefone.size() > 0) {
				this.paginadadosbasicosAnt = true;
			}
			if (this.telefone.size() > Integer.valueOf(mx.getQtdpesq())) {
				this.paginadadosbasicosProx = true;
				this.telefone.remove(this.telefone.size() - 1);
			}

			if ((mx.getF_telefone_fixos_celulares() != null) && (mx.getF_telefone_fixos_celulares())) {
				celulares = new ArrayList<>();
				fixos = new ArrayList<>();

				// if (telefone.get(0).getNumero().isEmpty()) {
				// for (Telefone telefone : telefone) {
				// fixos.add(telefone);
				// celulares.add(telefone);
				// }
				//
				// }

				/*
				 * SMarcio - faz um tratamento para os cpfs que nao possuem telefone para nao
				 * dar erro
				 */
				if (telefone.size() > 0 && !telefone.get(0).getNumeroTelefone().equals("")) {

					for (Telefone telefone : this.telefone) {

						// Rodrigo Almeida / Noelle Silveira - 25/07/2017 -
						if (telefone.getTipo() != null) {
							if (telefone.getTipo().equals("3")) {
								fixos.add(telefone);
							} else {
								celulares.add(telefone);
							}
						} else {
							//// Rodrigo Almeida / Noelle Silveira - 25/07/2017
							//// - Não estava sendo exibido o layout quando os
							//// CPF´s não possuiam telefone
							fixos.add(telefone);
							celulares.add(telefone);
						}
					}
				}

			} else {
				celulares = new ArrayList<>();
				fixos = new ArrayList<>();
			}

			this.setPaginadadosbasicos(true);

		} catch (SQLException e) {
			logger.error("processaConsulta - " + e.getMessage() + " - Consulta: " + consulta.getSql());
			achou = false;

		} finally {
			if (rs != null && !rs.isClosed())
				rs.close();
			if (stmtN != null && !stmtN.isClosed())
				stmtN.close();
			releaseConnection();
		}

		if (ind > 0 && isConsultaCpfCnpjValida()) {
			achou = true;
		} else {
			achou = false;
		}

		if (achou) {
			boolean _alertas = possuiAlertas(cpfcnpj);
			boolean _restricao = possuiRestricao(cpfcnpj);

			if (_restricao) {
				this.infocomplementares.setCreditoIcone("images/possui-restricao.png");
				this.infocomplementares.setCreditoMensagem(" Com restrição ");
			} else if (_alertas && !_restricao) {
				this.infocomplementares.setCreditoIcone("images/possui-alerta.png");
				this.infocomplementares.setCreditoMensagem(" Nada consta - com alerta ");
			} else {
				this.infocomplementares.setCreditoIcone("images/nada-consta.png");
				this.infocomplementares.setCreditoMensagem(" Nada consta ");
			}
		}

		return achou;

	}

	public String getWhatsApp(String telefone) {

		/*
		 * getWhatsApp
		 *
		 * Verifica se o cliente possui whatsApp By SMarcio 28/01/2016
		 *
		 */
		Connection conn = this.getConnection();
		java.sql.Statement stmtN;
		ResultSet rs;
		String sql = "";

		String retorno = "";

		try {

			sql = "SELECT FLAG FROM TBL_WHATSAPP WHERE TELEFONE = '" + telefone + "'";
			stmtN = conn.createStatement();
			rs = stmtN.executeQuery(sql);
			while (rs != null && rs.next()) {

				retorno = this.Filtra(rs.getString("FLAG") == null ? "" : rs.getString("FLAG"));

			}

			stmtN.close();
			rs.close();

		} catch (Exception e) {

			retorno = "";

		}

		return retorno;

	}

	public List<Telefone> findTelefones(LoginMBean mx, String query) {
		java.sql.Statement stmtN;
		ResultSet rs;
		int ind = 0;
		String aux = "";
		String anobase = "";
		int atualizacaoTel = 0;
		int atualizacaoBase = 0;
		String nome = "";
		String celular = "";
		String dtn = "";
		String dts = "";
		String ets = "";
		String areaterreno = "";
		String basecalc = "";
		String diavencimento = "";
		String fracaoideal = "";
		String daincl = "";
		String dalice = "";
		StringBuilder sql = new StringBuilder();
		String sexo = "";
		String dtabertura = "";
		String cpfcnpj = "";
		Connection conn = this.getConnection();
		List<Telefone> telefones = new ArrayList<Telefone>();

		try {
			stmtN = conn.createStatement();
			rs = stmtN.executeQuery(query);
			ind = 0;
			int id = 1;
			String nasc = "";
			String emails = "";
			Telefone t;
			String dtobito = "";

			while (rs != null && rs.next()) {

				t = new Telefone();

				if (ind == 0) {
					t.setEoprimeiro(true);
				}

				t.setId(Integer
						.parseInt(this.Filtra(rs.getString("PAGINA_RN") == null ? "" : rs.getString("PAGINA_RN"))));
				t.setProprietario(
						this.Filtra(rs.getString("PROPRIETARIO") == null ? "" : rs.getString("PROPRIETARIO")));
				t.setUf(this.Filtra(rs.getString("UF") == null ? "" : rs.getString("UF")));
				t.setCpfcnpj(cpfcnpj);
				t.setNumeroTelefone(
						this.Filtra(rs.getString("TELEFONE") == null ? "-----------" : rs.getString("TELEFONE")));

				/*
				 * Verifica se o cliente possui direito a base que ele esta consultando, se não
				 * mostra alguns dados mas os endereços e telefones não mostra
				 */

				if (Conexao.GetSerial(conn, mx.getUsuario().getLogin()).indexOf(t.getUf()) != -1) {

					t.setNumeroTelefone(
							this.Filtra(rs.getString("TELEFONE") == null ? "-----------" : rs.getString("TELEFONE")));
					t.setEndereco(this.Filtra(rs.getString("ENDERECO") == null ? "" : rs.getString("ENDERECO")));
					t.setNumero(this.Filtra(rs.getString("NUMERO") == null ? "" : rs.getString("NUMERO")));
					t.setComplemento(
							this.Filtra(rs.getString("COMPLEMENTO") == null ? "" : rs.getString("COMPLEMENTO")));
					t.setBairro(this.Filtra(rs.getString("BAIRRO") == null ? "" : rs.getString("BAIRRO")));
					t.setCidade(this.Filtra(rs.getString("CIDADE") == null ? "" : rs.getString("CIDADE")));
					t.setCep(this.Filtra(rs.getString("CEP") == null ? "" : rs.getString("CEP")));
					t.setProcon(this.proconSP(t.getNumeroTelefone(), conn));
					if (t.getNumeroTelefone().length() < 9) {
						t.setNumeroTelefone("-----------");
					}

				} else {

					t.setNumeroTelefone("--Estado não contratado.");
					t.setEndereco("Estado não contratado ");
					t.setNumero("-");
					t.setComplemento("-");
					t.setBairro("-");
					t.setCidade("-");
					t.setCep("-");
					t.setProcon("-");

				}
				t.setStatusLinha(this.Filtra(rs.getString("STATUS_LINHA") == null ? "" : rs.getString("STATUS_LINHA")));
				t.setAtual(this.Filtra(rs.getString("ATUAL") == null ? "" : rs.getString("ATUAL")));

				t.setRatingTelefone(t.getAtual(), getUseStatus());
				t.setWhatsApp(rs.getString("WHATSAPP"));
				telefones.add(t);

				ind++;
				id++;

			}
			stmtN.close();
			rs.close();

		} catch (SQLException e) {
			telefones = telefone;
		} finally {
			releaseConnection();
		}

		return telefones;
	}

	public String obito(String cpfcnpj, Connection comm) {

		String obret = "(NÃO CONSTA ÓBITO)";
		// String obret = "(NÃO CONSTA ÓBITO)";
		String SQL = null;
		String dtobito = "";

		try {

			SQL = "SELECT DT_OBITO FROM OBITO WHERE NU_CPF = ? ";
			java.sql.PreparedStatement stmtN = comm.prepareStatement(SQL);
			stmtN.setString(1, cpfcnpj);
			ResultSet rs = stmtN.executeQuery();
			if (rs != null && rs.next()) {

				try {

					dtobito = rs.getString("DT_OBITO");
					dtobito = dtobito.substring(6, 8) + "/" + dtobito.substring(4, 6) + "/" + dtobito.substring(0, 4);

				} catch (Exception e) {
					logger.error("Erro no metodo obito da classe Resposta: " + e.getMessage());
					obret = "";

				}

				obret = "( ÓBITO EM " + dtobito + " )";

			}
			if (rs != null && !rs.isClosed())
				rs.close();
			if (stmtN != null && !stmtN.isClosed())
				stmtN.close();
			return obret;

		} catch (SQLException e) {
			logger.error("Erro no metodo obito da classe Resposta: " + e.getMessage());
			return "( NÃO CONSTA ÓBITO )";

		}
	}

	public Boolean processaConsultaTelefonesComerciais(SqlToBind consulta, int tipopesquisa, LoginMBean mx)
			throws SQLException {

		/*
		 * Método de pesquisa telefones comerciais
		 *
		 * By SMarcio em 05/11/2013
		 */

		java.sql.PreparedStatement stmtN = null;
		ResultSet rs = null;
		int ind = 0;
		String aux = "";
		String anobase = "";
		int atualizacaoTel = 0;
		int atualizacaoBase = 0;
		String nome = "";
		String celular = "";
		String dtn = "";
		String dts = "";
		String ets = "";
		String areaterreno = "";
		String basecalc = "";
		String diavencimento = "";
		String fracaoideal = "";
		String daincl = "";
		String dalice = "";
		String sql = "";
		String sexo = "";
		String dtabertura = "";
		String cpfcnpj = "";
		String cpfcnpjant = "";
		Connection conn = this.getConnection();
		Boolean achou = false;
		Boolean requestFromViewSingleButton = false;
		if (!possuiConexao) {
			setPossuiConexao(true);
			requestFromViewSingleButton = true;
		}
		try {

			stmtN = conn.prepareStatement(consulta.getSql());

			for (int i = 0; i <= consulta.getBinds().size() - 1; i++)
				stmtN.setString(i + 1, consulta.getBinds().get(i));

			rs = stmtN.executeQuery();
			ind = 0;
			int id = 0;
			String nasc = "";
			String emails = "";
			TelefonesComerciais t;
			String dtobito = "";
			Infocomplementares info;

			if (tipopesquisa == 1) {

				this.telefoneComercial = new ArrayList<TelefonesComerciais>();

			}

			while (rs.next()) {
				id++;
				t = new TelefonesComerciais();
				info = new Infocomplementares();
				/*
				 * Query: STATUS_LINHA, "; DT_ABERTURA, "; SITUACAO, "; FANTASIA,
				 * "; NATUREZA, "; T.PROPRIETARIO T.ATUAL, T.TELEFONE, T.ENDERECO, T.NUMERO
				 * T.COMPLEMENTO, T.BAIRRO, T.CEP, T.CIDADE, T.UF, T.CPFCGC, TITULO,
				 * T.OPERADORA, DT_INSTALACAO
				 *
				 */

				/* Pesquisa CpfCnpj ConfirmeOnLine New */

				cpfcnpj = this.Filtra(rs.getString("CPFCNPJ") == null ? "" : rs.getString("CPFCNPJ"));

				if (!cpfcnpj.equals(cpfcnpjant)) {

					t.setEoprimeiro(true);
					cpfcnpjant = cpfcnpj;
					id = 1;
				}
				nome = this.Filtra(rs.getString("PROPRIETARIO") == null ? "" : rs.getString("PROPRIETARIO"));

				if (nome.equals("")) {

					nome = this.Filtra(rs.getString("PROPRIETARIO") == null ? "" : rs.getString("PROPRIETARIO"));

				}
				info.setNome(nome);
				info.setCpfcnpj(cpfcnpj);
				String birthday = dao.findBirthday(cpfcnpj);
				Integer idade = Utils.calculaIdade(birthday);
				info.setDtnasc(birthday != null ? birthday + " - " + idade + " anos." : null);
				t.setLabel1("Dt. Fundação:");
				info.setSigno(this.Filtra(rs.getString("FANTASIA") == null ? "" : rs.getString("FANTASIA")));
				t.setLabel2("Nome Fantasia:");
				info.setSexo(this.Filtra(rs.getString("NATUREZA") == null ? "" : rs.getString("NATUREZA")));
				t.setLabel3("Natureza:");
				info.setNomemae(this.Filtra(rs.getString("SITUACAO") == null ? "" : rs.getString("SITUACAO")));
				t.setLabel4("Situação:");
				t.setLabel5("");
				info.setCpfcnpj(this.Filtra(rs.getString("CPFCNPJ") == null ? "" : rs.getString("CPFCNPJ")));
				t.setInfotelefone(info);
				t.setId(id);
				t.setProprietario(
						this.Filtra(rs.getString("PROPRIETARIO") == null ? "" : rs.getString("PROPRIETARIO")));
				t.setEndereco(this.Filtra(rs.getString("ENDERECO") == null ? "" : rs.getString("ENDERECO")));
				t.setNumero(this.Filtra(rs.getString("NUMERO") == null ? "" : rs.getString("NUMERO")));
				t.setComplemento(this.Filtra(rs.getString("COMPLEMENTO") == null ? "" : rs.getString("COMPLEMENTO")));
				t.setBairro(this.Filtra(rs.getString("BAIRRO") == null ? "" : rs.getString("BAIRRO")));
				t.setCidade(this.Filtra(rs.getString("CIDADE") == null ? "" : rs.getString("CIDADE")));
				t.setCep(this.Filtra(rs.getString("CEP") == null ? "" : rs.getString("CEP")));
				t.setUf(this.Filtra(rs.getString("UF") == null ? "" : rs.getString("UF")));
				t.setCpfcnpj(cpfcnpj);
				t.setNumeroTelefone(
						this.Filtra(rs.getString("TELEFONE") == null ? "-----------" : rs.getString("TELEFONE")));

				t.setStatusLinha(this.Filtra(rs.getString("STATUS_LINHA") == null ? "" : rs.getString("STATUS_LINHA")));
				t.setAtual(this.Filtra(rs.getString("ATUAL") == null ? "" : rs.getString("ATUAL")));

				/*
				 * Verifica se o cliente possui direito a base que ele esta consultando, se não
				 * mostra alguns dados mas os endereços e telefones não mostra
				 */

				if (Conexao.GetSerial(conn, mx.getUsuario().getLogin()).indexOf(t.getUf()) != -1) {

					t.setNumeroTelefone(
							this.Filtra(rs.getString("TELEFONE") == null ? "-----------" : rs.getString("TELEFONE")));
					t.setEndereco(this.Filtra(rs.getString("ENDERECO") == null ? "" : rs.getString("ENDERECO")));
					t.setNumero(this.Filtra(rs.getString("NUMERO") == null ? "" : rs.getString("NUMERO")));
					t.setComplemento(
							this.Filtra(rs.getString("COMPLEMENTO") == null ? "" : rs.getString("COMPLEMENTO")));
					t.setBairro(this.Filtra(rs.getString("BAIRRO") == null ? "" : rs.getString("BAIRRO")));
					t.setCidade(this.Filtra(rs.getString("CIDADE") == null ? "" : rs.getString("CIDADE")));
					t.setCep(this.Filtra(rs.getString("CEP") == null ? "" : rs.getString("CEP")));
					t.setProcon(this.proconSP(t.getNumeroTelefone(), conn));
					if (t.getNumeroTelefone().length() < 9) {

						t.setNumeroTelefone("-----------");

					}

				} else {

					t.setNumeroTelefone("--Estado não contratado.");
					t.setEndereco("Estado não contratado ");
					t.setNumero("-");
					t.setComplemento("-");
					t.setBairro("-");
					t.setCidade("-");
					t.setCep("-");
					t.setProcon("-");

				}

				t.setRatingTelefone(t.getAtual(), getUseStatus());
				t.setWhatsApp(rs.getString("WHATSAPP"));
				this.telefoneComercial.add(t);

				ind++;

			}
			if (rs != null && !rs.isClosed())
				rs.close();
			if (stmtN != null && !stmtN.isClosed())
				stmtN.close();

			this.setPaginatelefonescomerciaisAnt(false);
			this.setPaginatelefonescomerciaisProx(false);
			if (!(mx.getPaginaTelCom().equals(1)) && this.telefoneComercial.size() > 0) {
				this.setPaginatelefonescomerciaisAnt(true);
			}
			if (this.telefoneComercial.size() > Integer.valueOf(mx.getQtdpesq())) {
				this.setPaginatelefonescomerciaisProx(true);
				this.telefoneComercial.remove(this.telefoneComercial.size() - 1);
			}

		} catch (SQLException e) {
			logger.error("Erro no metodo processaConsultaTelefonesComerciais da classe resposta: " + e.getMessage());
			achou = false;
		} finally {
			if (requestFromViewSingleButton) {
				setPossuiConexao(false);
			}
			releaseConnection();

			if (rs != null)
				rs.close();
			if (stmtN != null)
				stmtN.close();
		}
		if (ind > 0) {
			achou = true;
		} else {
			achou = false;
		}
		return achou;

	}

	public Boolean processaConsultaVizinhos(SqlToBind consulta, int tipopesquisa, LoginMBean mx, Integer paginaInicial,
			Integer paginaFinal) throws SQLException {
		/*
		 * MÉtodo de pesquisa Vizinhos
		 *
		 * By SMarcio em 05/11/2013
		 */

		CallableStatement stmt = null;
		ResultSet rs = null;
		int ind = 0;
		String aux = "";
		String anobase = "";
		int atualizacaoTel = 0;
		int atualizacaoBase = 0;
		String nome = "";
		String celular = "";
		String dtn = "";
		String dts = "";
		String ets = "";
		String areaterreno = "";
		String basecalc = "";
		String diavencimento = "";
		String fracaoideal = "";
		String daincl = "";
		String dalice = "";
		String sql = "";
		String sexo = "";
		String dtabertura = "";
		String cpfcnpj = "";
		String cpfcnpjant = "";

		protecaoDados = false;
		menorDeIdade=false;

		Connection conn = this.getConnection();
		Boolean requestFromViewSingleButton = false;
		if (!possuiConexao) {
			setPossuiConexao(true);
			requestFromViewSingleButton = true;
		}
		Boolean achou = false;

		try {

			if (tipopesquisa == 3) {
				String paramNome = mx.getPessoaSite().getNome();
				int i = 1;
				// conn=((DelegatingConnection)conn).getInnermostDelegate();
				stmt = conn.prepareCall("BEGIN CONFIRME_PESQUISA_NOME_CPF(?,?,?,?,?,?,?,?,?,?,?); END;");
				String dataNasc = formatDate(mx.getPessoaSite().getDateNasc(), "dd/MM/yyyy");
				String nomeFiltrado = Conexao.Filtra(paramNome);
				stmt.setString(i++, nomeFiltrado);
				stmt.setString(i++, mx.getPessoaSite().getEndereco().getUf() == null ? ""
						: mx.getPessoaSite().getEndereco().getUf().toString());
				stmt.setString(i++, mx.getPessoaSite().getEndereco().getCidade());
				stmt.setString(i++, mx.getPessoaSite().getEndereco().getBairro());
				stmt.setString(i++, dataNasc);
				stmt.setString(i++, mx.getPessoaSite().getNomeMae() == null ? "" : mx.getPessoaSite().getNomeMae());
				stmt.setString(i++, mx.getUsuario().getLogin());
				stmt.setInt(i++, paginaInicial);
				stmt.setInt(i++, paginaFinal);
				stmt.setInt(i++, Integer.valueOf(mx.getQtdpesq()));
				stmt.registerOutParameter(11, OracleTypes.CURSOR);
				stmt.execute();
				rs = (ResultSet) stmt.getObject(11);

			} else if (tipopesquisa == 6) {

				String paramNome = mx.getPessoaSite().getNome();
				int i = 1;
				// conn=((DelegatingConnection)conn).getInnermostDelegate();
				stmt = conn.prepareCall("BEGIN CONFIRME_PESQUISA_NOME_CNPJ(?,?,?,?,?,?,?,?,?); END;");
				String dataAbertura = formatDate(mx.getPessoaSite().getDateNasc(), "dd/MM/yyyy");
				String nomeFiltrado = Conexao.Filtra(paramNome);
				stmt.setString(i++, nomeFiltrado);
				stmt.setString(i++, mx.getPessoaSite().getEndereco().getUf() == null ? ""
						: mx.getPessoaSite().getEndereco().getUf().toString());
				stmt.setString(i++, mx.getPessoaSite().getEndereco().getCidade());
				stmt.setString(i++, mx.getPessoaSite().getEndereco().getBairro());
				stmt.setString(i++, dataAbertura);
				stmt.setInt(i++, paginaInicial);
				stmt.setInt(i++, paginaFinal);
				stmt.setInt(i++, Integer.valueOf(mx.getQtdpesq()));
				stmt.registerOutParameter(i, OracleTypes.CURSOR);
				stmt.execute();
				rs = (ResultSet) stmt.getObject(i);

			} else {
				stmt = conn.prepareCall(consulta.getSql());

				for (int i = 0; i <= consulta.getBinds().size() - 1; i++)
					stmt.setString(i + 1, consulta.getBinds().get(i));

				rs = stmt.executeQuery();
			}

			ind = 0;
			int id = 0;
			String nasc = "";
			String emails = "";
			Vizinhos t;
			String dtobito = "";
			Infocomplementares info;

			// if ( tipopesquisa == 1 ){

			this.vizinhos = new ArrayList<Vizinhos>();

			// }

			while (rs != null && rs.next()) {
				
				
//                Rodrigo Almeida 28/10/2019
//                Verificando se um determinado CPF está relacionado na tabela TB_PROTECAO_DADOS_PESSOAIS
				this.protecaoDados = false;
				if (tipopesquisa != 6)
					this.protecaoDados = verificaProtecaoDadosPessoais(rs.getString("CPFCNPJ"), "");
				else
					this.protecaoDados = false;
				
				
					String birthday=null;
					Integer idade=null;
					menorDeIdade=false;
					if (tipopesquisa != 6) {
						cpfcnpj = this.Filtra(rs.getString("CPFCNPJ") == null ? "" : rs.getString("CPFCNPJ"));
						birthday = dao.findBirthday(cpfcnpj);
						idade = Utils.calculaIdade(birthday);
					
						if (idade == null) {
							idade=18;
						}
						
						if (idade < 18 && cpfcnpj.length() == 11) {
							menorDeIdade=true;
						}
					}
					
				if (!this.protecaoDados) {
					if (menorDeIdade==false) {
						
					
						List<String> list;
						id++;
						t = new Vizinhos();
						info = new Infocomplementares();
	
						if (tipopesquisa != 6)
							cpfcnpj = this.Filtra(rs.getString("CPFCNPJ") == null ? "" : rs.getString("CPFCNPJ"));
						else {
							cpfcnpj = this.Filtra(rs.getString("CNPJ") == null ? "" : rs.getString("CNPJ"));
						}
	
						if (mx.getDynamoDBService().isPpe(cpfcnpj)) {
							continue;
						}
						t.setCpfcnpj(cpfcnpj);
						if (!cpfcnpj.equals(cpfcnpjant)) {
							t.setEoprimeiro(true);
							cpfcnpjant = cpfcnpj;
							id = 1;
						}
	
						if (tipopesquisa != 6) {
							nome = this.Filtra(rs.getString("NOME") == null ? "" : rs.getString("NOME").trim());
							if (nome.equals("")) {
								nome = this
										.Filtra(rs.getString("PROPRIETARIO") == null ? "" : rs.getString("PROPRIETARIO"));
							}
						} else
							nome = this.Filtra(rs.getString("PROPRIETARIO") == null ? "" : rs.getString("PROPRIETARIO"));
						info.setNome(nome);
	
						if (tipopesquisa != 6) {
							sexo = this.Filtra(GetSexo(nome, cpfcnpj));
							if (sexo.equals("M")) {
	
								sexo = "MASCULINO";
	
							}
							if (sexo.equals("F")) {
	
								sexo = "FEMININO";
	
							}
						}
	//					String birthday = dao.findBirthday(cpfcnpj);
	//					Integer idade = Utils.calculaIdade(birthday);
						// this.infocomplementares.setDtnasc(rs.getString("NASC") ==
						// null ? "" : rs.getString("NASC")+idade);
						info.setDtnasc(birthday != null ? birthday + " - " + idade + " anos." : null);
						if (cpfcnpj.length() < 14 && t.getEoprimeiro()) {
							// String idade = Utils.calculaIdade(rs.getString("NASC"),
							// "dd/MM/yyyy");
							// info.setDtnasc(rs.getString("NASC") == null ? "" :
							// rs.getString("NASC")+idade);
							info.setSigno(Utils.findSigno(info.getDtnasc(), "dd/MM/yyyy"));
							info.setSexo(sexo);
							info.setNomemae(this.Filtra(rs.getString("MAE") == null ? "" : rs.getString("MAE")));
							info.setNomePai(this.Filtra(rs.getString("PAI") == null ? "" : rs.getString("PAI")));
							info.setNomeConjuge(
									this.Filtra(rs.getString("NOME_CONJUGE") == null ? "" : rs.getString("NOME_CONJUGE")));
							info.setCpfConjuge(
									this.Filtra(rs.getString("CPF_CONJUGE") == null ? "" : rs.getString("CPF_CONJUGE")));
							info.setTituloEleitor(this.getTituloEleitoral(cpfcnpj, this.getConnection()));
							/*
							 * dtobito = this.Filtra(rs.getString("OBITO") == null ? "" :
							 * rs.getString("OBITO"));
							 */
							dtobito = ObitoDao.findDataObito(mx, cpfcnpj, nome, conn);
							info.setDtobito(dtobito);
	
						}
						if (cpfcnpj.length() == 14 && t.getEoprimeiro()) {
							
//							-------------------------------------------------------------------------------------------------------------
//							Rodrigo Almeida - 14/04/2020 - Corrigindo o problema da não exbição da data de abertura e da Razão Social
							birthday = dao.findBirthday(cpfcnpj);
							idade = Utils.calculaIdade(birthday);
							dtabertura = (birthday != null ? birthday + " - " + idade + " anos." : null);
							
							info.setDtnasc(dtabertura);
	
							if (rs.getString("PROPRIETARIO") !=null) {
								info.setNome(this.Filtra(rs.getString("PROPRIETARIO") == null ? "" : rs.getString("PROPRIETARIO")));
							}else {
								
								info.setNome(this.Filtra(rs.getString("RAZAO_SOCIAL") == null ? "" : rs.getString("RAZAO_SOCIAL")));
							}
//							-------------------------------------------------------------------------------------------------------------
							
							list = getQsaEmpresasInfo(cpfcnpj);
	
							info.setSigno(this.Filtra(list.get(2) == null ? "" : list.get(2)));
							info.setSexo(this.Filtra(list.get(3) == null ? "" : list.get(3)));
							info.setNomemae(this.Filtra(list.get(1) == null ? "" : list.get(1)));
							info.setRamoAtvi(this.Filtra(list.get(4) == null ? "" : list.get(4)));
	
						}
	
						info.setCpfcnpj(cpfcnpj);
						t.setInfotelefone(info);
						if (t.getEoprimeiro()) {
							t.setSocioSociedades(findSocioSociedadesByCpfcgc(cpfcnpj));
							t.setEmails(findEmailsByCpfcgc(cpfcnpj));
						}
						t.setId(id);
						t.setProprietario(nome);
						t.setEndereco(this.Filtra(rs.getString("ENDERECO") == null ? "" : rs.getString("ENDERECO")));
						t.setNumero(this.Filtra(rs.getString("NUMERO") == null ? "" : rs.getString("NUMERO")));
						t.setComplemento(
								this.Filtra(rs.getString("COMPLEMENTO") == null ? "" : rs.getString("COMPLEMENTO")));
						t.setBairro(this.Filtra(rs.getString("BAIRRO") == null ? "" : rs.getString("BAIRRO")));
						t.setCidade(this.Filtra(rs.getString("CIDADE") == null ? "" : rs.getString("CIDADE")));
						t.setCep(this.Filtra(rs.getString("CEP") == null ? "" : rs.getString("CEP")));
						t.setUf(this.Filtra(rs.getString("UF") == null ? "" : rs.getString("UF")));
						t.setCpfcnpj(cpfcnpj);
						t.setNumeroTelefone(
								this.Filtra(rs.getString("TELEFONE") == null ? "-----------" : rs.getString("TELEFONE")));
						t.setStatusLinha(
								this.Filtra(rs.getString("STATUS_LINHA") == null ? "" : rs.getString("STATUS_LINHA")));
						t.setAtual(this.Filtra(rs.getString("ATUAL") == null ? "" : rs.getString("ATUAL")));
						t.setOperadora(this.Filtra(rs.getString("OPERADORA") == null ? "" : rs.getString("OPERADORA")));
						/*
						 * Verifica se o cliente possui direito a base que ele esta consultando, se não
						 * mostra alguns dados mas os endereços e telefones não mostra
						 */
	
						if (conn.isClosed() == true) {
							conn = this.getConnection();
						}
	
						if (Conexao.GetSerial(conn, mx.getUsuario().getLogin()).indexOf(t.getUf()) != -1) {
	
							t.setNumeroTelefone(this
									.Filtra(rs.getString("TELEFONE") == null ? "-----------" : rs.getString("TELEFONE")));
							t.setEndereco(this.Filtra(rs.getString("ENDERECO") == null ? "" : rs.getString("ENDERECO")));
							t.setNumero(this.Filtra(rs.getString("NUMERO") == null ? "" : rs.getString("NUMERO")));
							t.setComplemento(
									this.Filtra(rs.getString("COMPLEMENTO") == null ? "" : rs.getString("COMPLEMENTO")));
							t.setBairro(this.Filtra(rs.getString("BAIRRO") == null ? "" : rs.getString("BAIRRO")));
							t.setCidade(this.Filtra(rs.getString("CIDADE") == null ? "" : rs.getString("CIDADE")));
							t.setCep(this.Filtra(rs.getString("CEP") == null ? "" : rs.getString("CEP")));
							t.setProcon(this.proconSP(t.getNumeroTelefone(), conn));
							if (t.getNumeroTelefone().length() < 9) {
								t.setNumeroTelefone("-----------");
							}
	
						} else {
	
							t.setNumeroTelefone("--Estado não contratado.");
							t.setEndereco("Estado não contratado ");
							t.setNumero("-");
							t.setComplemento("-");
							t.setBairro("-");
							t.setCidade("-");
							t.setCep("-");
							t.setProcon("-");
	
						}
						t.setRatingTelefone(t.getAtual(), getUseStatus());
						t.setWhatsApp(rs.getString("WHATSAPP"));
						t.setLabels();
						this.vizinhos.add(t);
						ind++;
						
						
					} // menorDeIdade 
				
				} //protecaoDados

			} //while
			// stmtN.close();

			this.paginavizinhosAnt = false;
			this.paginavizinhosProx = false;
			if (!(mx.getPaginaVizinho().equals(1)) && this.vizinhos.size() > 0) {
				this.paginavizinhosAnt = true;
			}
			if (this.vizinhos.size() > Integer.valueOf(mx.getQtdpesq())) {
				this.paginavizinhosProx = true;
				this.vizinhos.remove(this.vizinhos.size() - 1);
			}
			this.vizinhos = removeDuplicadas(vizinhos);
			// Collections.sort(vizinhos,new Util.CpfcgcComparator());

		} catch (Exception e) {
			logger.error("Erro no metodo processaConsultaVizinhos da classe Resposta: " + e.getMessage());

			mx.setResposta_consulta(true);
			mx.setResposta_conArmazenada(true);
			mx.setResposta_endereco(false);
			mx.setResposta_endereco(false);
			mx.setResposta_operadora(false);
			mx.setResposta_nome(false);
			mx.setResposta_razao(false);
			mx.setResposta_cep(false);
			mx.setResposta_veiculo(false);
			mx.setResposta_historico_credito(false);
			achou = false;
		} finally {
			if (requestFromViewSingleButton) {
				setPossuiConexao(false);
			}
			releaseConnection();

			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			if (conn != null)
				conn.close();
		}

		if (ind > 0) {

			achou = true;

		} else {
			mx.setResposta_consulta(true);
			mx.setResposta_conArmazenada(false);
			mx.setResposta_endereco(false);
			mx.setResposta_endereco(false);
			mx.setResposta_operadora(false);
			mx.setResposta_nome(false);
			mx.setResposta_razao(false);
			mx.setResposta_cep(false);
			mx.setResposta_veiculo(false);
			mx.setResposta_historico_credito(false);
			mx.setResposta_obitoNacional(false);
			achou = false;
		}

		return achou;

	}

	public Boolean processaConsultaFilhos(SqlToBind consulta, int tipopesquisa, LoginMBean mx, String qtdpesq)
			throws SQLException {

		java.sql.PreparedStatement stmtN = null;
		ResultSet rs = null;
		int ind = 0;
		String aux = "";
		String anobase = "";
		int atualizacaoTel = 0;
		int atualizacaoBase = 0;
		String nome = "";
		String celular = "";
		String dtn = "";
		String dts = "";
		String ets = "";
		String areaterreno = "";
		String basecalc = "";
		String diavencimento = "";
		String fracaoideal = "";
		String daincl = "";
		String dalice = "";
		String sql = "";
		String sexo = "";
		String dtabertura = "";
		String cpfcnpj = "";
		String cpfcnpjant = "";
		Connection conn = this.getConnection();
		Boolean achou = false;
		Boolean requestFromViewSingleButton = false;
		if (!possuiConexao) {
			setPossuiConexao(true);
			requestFromViewSingleButton = true;
		}
		try {
			stmtN = conn.prepareStatement(consulta.getSql());
			// stmtN.setQueryTimeout(20);

			for (int i = 0; i <= consulta.getBinds().size() - 1; i++)
				stmtN.setString(i + 1, consulta.getBinds().get(i));

			rs = stmtN.executeQuery();
			ind = 0;
			int id = 0;
			String nasc = "";
			String emails = "";
			Parentes t;
			String dtobito = "";
			Infocomplementares info;

			if (tipopesquisa == 1) {

				this.filhos = new ArrayList<Parentes>();

			}

			while (rs != null && rs.next()) {
				id++;
				t = new Parentes();
				info = new Infocomplementares();
				/*
				 * tipos de pesquisa 1 - pesquisa por telefone 2 - parentes
				 *
				 */

				/* Pesquisa CpfCnpj ConfirmeOnLine New */

				cpfcnpj = this.Filtra(rs.getString("CPFCNPJ") == null ? "" : rs.getString("CPFCNPJ"));
				List<String> list = getQsaEmpresasInfo(cpfcnpj);

				if (!cpfcnpj.equals(cpfcnpjant)) {

					t.setEoprimeiro(true);
					cpfcnpjant = cpfcnpj;
					id = 1;
				}
				nome = this.Filtra(rs.getString("NOME") == null ? "" : rs.getString("NOME"));

				if (nome.equals("")) {

					nome = this.Filtra(rs.getString("PROPRIETARIO") == null ? "" : rs.getString("PROPRIETARIO"));

				}

				info.setNome(nome);

				sexo = this.Filtra(GetSexo(rs.getString("PROPRIETARIO") == null ? "" : rs.getString("PROPRIETARIO"),
						rs.getString("CPFCNPJ") == null ? "" : rs.getString("CPFCNPJ")));

				if (sexo.equals("M")) {

					sexo = "MASCULINO";

				}
				if (sexo.equals("F")) {

					sexo = "FEMININO";

				}
				String birthday = dao.findBirthday(cpfcnpj);
				Integer idade = Utils.calculaIdade(birthday);
				// this.infocomplementares.setDtnasc(rs.getString("NASC") ==
				// null ? "" : rs.getString("NASC")+idade);
				info.setDtnasc(birthday != null ? birthday + " - " + idade + " anos." : null);

				if (cpfcnpj.length() < 14) {
					t.setLabel1("Dt. Nascimento:");

					info.setSigno(Utils.findSigno(info.getDtnasc(), "dd/MM/yyyy"));
					t.setLabel2("Signo:");

					info.setSexo(sexo);
					t.setLabel3("Sexo:");

					info.setNomemae(this.Filtra(rs.getString("MAE") == null ? "" : rs.getString("MAE")));
					t.setLabel4("Nome da Mãe:");

					info.setTituloEleitor(this.getTituloEleitoral(cpfcnpj, this.getConnection()));
					t.setLabel5("Título de Eleitor:");

					// Rodrigo Almeida - 21/07/2017
					t.setLabel6("Grau de Parentesco:");
					info.setGrpa_ds_parentesco(
							rs.getString("GRPA_DS_PARENTESCO") == null ? "" : rs.getString("GRPA_DS_PARENTESCO"));

				}

				if (cpfcnpj.length() == 14) {
					t.setLabel1("Dt. Fundação:");

					info.setSigno(this.Filtra(list.get(2) == null ? "" : list.get(2)));
					t.setLabel2("Nome Fantasia:");

					info.setSexo(this.Filtra(list.get(3) == null ? "" : list.get(3)));
					t.setLabel3("Natureza:");

					info.setNomemae(this.Filtra(list.get(1) == null ? "" : list.get(1)));
					t.setLabel4("Situação:");
					t.setLabel5("");

				}
				dtobito = this.Filtra(rs.getString("OBITO") == null ? "" : rs.getString("OBITO"));

				dtobito = ObitoDao.findDataObito(mx, cpfcnpj, nome, conn);
				info.setDtobito(dtobito);
				info.setCpfcnpj(this.Filtra(rs.getString("CPFCNPJ") == null ? "" : rs.getString("CPFCNPJ")));
				t.setInfotelefone(info);

				t.setId(id);
				t.setProprietario(
						this.Filtra(rs.getString("PROPRIETARIO") == null ? "" : rs.getString("PROPRIETARIO")));
				t.setEndereco(this.Filtra(rs.getString("ENDERECO") == null ? "" : rs.getString("ENDERECO")));
				t.setNumero(this.Filtra(rs.getString("NUMERO") == null ? "" : rs.getString("NUMERO")));
				t.setComplemento(this.Filtra(rs.getString("COMPLEMENTO") == null ? "" : rs.getString("COMPLEMENTO")));
				t.setBairro(this.Filtra(rs.getString("BAIRRO") == null ? "" : rs.getString("BAIRRO")));
				t.setCidade(this.Filtra(rs.getString("CIDADE") == null ? "" : rs.getString("CIDADE")));
				t.setCep(this.Filtra(rs.getString("CEP") == null ? "" : rs.getString("CEP")));
				t.setUf(this.Filtra(rs.getString("UF") == null ? "" : rs.getString("UF")));
				t.setCpfcnpj(cpfcnpj);
				t.setNumeroTelefone(
						this.Filtra(rs.getString("TELEFONE") == null ? "-----------" : rs.getString("TELEFONE")));

				t.setStatusLinha(this.Filtra(rs.getString("STATUS_LINHA") == null ? "" : rs.getString("STATUS_LINHA")));
				t.setAtual(this.Filtra(rs.getString("ATUAL") == null ? "" : rs.getString("ATUAL")));

				/*
				 * Verifica se o cliente possui direito a base que ele esta consultando, se não
				 * mostra alguns dados mas os endereços e telefones não mostra
				 */

				if (Conexao.GetSerial(conn, mx.getUsuario().getLogin()).indexOf(t.getUf()) != -1) {

					t.setNumeroTelefone(
							this.Filtra(rs.getString("TELEFONE") == null ? "-----------" : rs.getString("TELEFONE")));
					t.setEndereco(this.Filtra(rs.getString("ENDERECO") == null ? "" : rs.getString("ENDERECO")));
					t.setNumero(this.Filtra(rs.getString("NUMERO") == null ? "" : rs.getString("NUMERO")));
					t.setComplemento(
							this.Filtra(rs.getString("COMPLEMENTO") == null ? "" : rs.getString("COMPLEMENTO")));
					t.setBairro(this.Filtra(rs.getString("BAIRRO") == null ? "" : rs.getString("BAIRRO")));
					t.setCidade(this.Filtra(rs.getString("CIDADE") == null ? "" : rs.getString("CIDADE")));
					t.setCep(this.Filtra(rs.getString("CEP") == null ? "" : rs.getString("CEP")));
					t.setProcon(this.proconSP(t.getNumeroTelefone(), conn));
					if (t.getNumeroTelefone().length() < 9) {

						t.setNumeroTelefone("-----------");

					}

				} else {

					t.setNumeroTelefone("--Estado não contratado.");
					t.setEndereco("Estado não contratado ");
					t.setNumero("-");
					t.setComplemento("-");
					t.setBairro("-");
					t.setCidade("-");
					t.setCep("-");
					t.setProcon("-");

				}

				t.setRatingTelefone(t.getAtual(), getUseStatus());
				t.setWhatsApp(rs.getString("WHATSAPP"));
				this.filhos.add(t);

				ind++;

			}

			this.setPaginaFilhosAnt(false);
			this.setPaginaFilhosProx(false);
			if (!(mx.getPaginaFilhos().equals(1)) && this.filhos.size() > 0) {
				this.setPaginaFilhosAnt(true);
			}
			if (this.filhos.size() > Integer.valueOf(mx.getQtdpesq())) {
				this.setPaginaFilhosProx(true);
				this.filhos.remove(this.filhos.size() - 1);
			}

		}
		// catch(SQLTimeoutException e){
		// processaConsultaParentes(Consulta, tipopesquisa, mx);
		// }
		catch (SQLException e) {
			logger.error("Erro no metodo processaConsultaFilhos da classe resposta: " + e.getMessage());
			achou = false;
		} finally {
			if (requestFromViewSingleButton) {
				setPossuiConexao(false);
			}
			releaseConnection();

			if (rs != null)
				rs.close();
			if (stmtN != null)
				stmtN.close();
			if (conn != null)
				conn.close();
		}
		if (ind > 0) {
			achou = true;
		} else {
			achou = false;
		}
		return achou;

	}

	// public Boolean processaConsultaFilhos(String Consulta, int tipopesquisa,
	// LoginMBean mx, String qtdpesq) {
	// /*
	// * Método de pesquisa Vizinhos
	// *
	// * By SMarcio em 05/11/2013
	// */
	//
	// java.sql.Statement stmtN;
	// ResultSet rs;
	// int ind = 0;
	// String aux = "";
	// String anobase = "";
	// int atualizacaoTel = 0;
	// int atualizacaoBase = 0;
	// String nome = "";
	// String celular = "";
	// String dtn = "";
	// String dts = "";
	// String ets = "";
	// String areaterreno = "";
	// String basecalc = "";
	// String diavencimento = "";
	// String fracaoideal = "";
	// String daincl = "";
	// String dalice = "";
	// String sql = "";
	// String sexo = "";
	// String dtabertura = "";
	// String cpfcnpj = "";
	// String cpfcnpjant = "";
	// StringBuilder sql1 = new StringBuilder();
	// StringBuilder sql2 = new StringBuilder();
	// StringBuilder sql3 = new StringBuilder();
	// Connection conn = this.getConnection();
	// Boolean achou = false;
	// Boolean requestFromViewSingleButton = false;
	// if (!possuiConexao) {
	// setPossuiConexao(true);
	// requestFromViewSingleButton = true;
	// }
	//
	// try {
	//
	// stmtN = conn.createStatement();
	// rs = stmtN.executeQuery(Consulta);
	// ind = 0;
	// String nasc = "";
	// String emails = "";
	// Parentes filho;
	// String dtobito = "";
	// Infocomplementares info;
	//
	// // if ( tipopesquisa == 1 ){
	//
	// this.filhos = new ArrayList<Parentes>();
	//
	// // }
	//
	// while (rs.next()) {
	// filho = new Parentes();
	// info = new Infocomplementares();
	//
	//
	//// filho.setIdade(Utils.calculaIdade(rs.getString("NASC")));
	// cpfcnpj = this.Filtra(rs.getString("CPFCNPJ") == null ? "" :
	// rs.getString("CPFCNPJ"));
	//
	// if (!cpfcnpj.equals(cpfcnpjant)) {
	// filho.setEoprimeiro(true);
	// cpfcnpjant = cpfcnpj;
	// }
	// nome = this.Filtra(rs.getString("NOME") == null ? "" :
	// rs.getString("NOME"));
	//
	// if (nome.equals("")) {
	// nome = this.Filtra(rs.getString("PROPRIETARIO") == null ? "" :
	// rs.getString("PROPRIETARIO"));
	// }
	//
	// info.setNome(nome);
	//
	// sexo = this.Filtra(GetSexo(nome, cpfcnpj, conn));
	//
	// if (sexo.equals("M")) {
	//
	// sexo = "MASCULINO";
	//
	// }
	// if (sexo.equals("F")) {
	//
	// sexo = "FEMININO";
	//
	// }
	// String birthday = dao.findBirthday(cpfcnpj);
	// Integer idade = Utils.calculaIdade(birthday);
	// info.setDtnasc(birthday + " - " + idade + " anos.");
	//
	// if (cpfcnpj.length() < 14) {
	// filho.setLabel1("Dt. Nascimento:");
	//
	// info.setSigno(Utils.findSigno(info.getDtnasc(), "dd/MM/yyyy"));
	// filho.setLabel2("Signo:");
	//
	// info.setSexo(sexo);
	// filho.setLabel3("Sexo:");
	//
	// info.setNomemae(this.Filtra(rs.getString("MAE") == null ? "" :
	// rs.getString("MAE")));
	// filho.setLabel4("Nome da Mãe:");
	//
	// info.setTituloEleitor(this.getTituloEleitoral(cpfcnpj,
	// this.getConnection()));
	// filho.setLabel5("Título de Eleitor:");
	// }
	//
	// if (cpfcnpj.length() == 14) {
	// filho.setLabel1("Dt. Fundação:");
	//
	// info.setSigno(this.Filtra(rs.getString("FANTASIA") == null ? "" :
	// rs.getString("FANTASIA")));
	// filho.setLabel2("Nome Fantasia:");
	//
	// info.setSexo(this.Filtra(rs.getString("NATUREZA") == null ? "" :
	// rs.getString("NATUREZA")));
	// filho.setLabel3("Natureza:");
	//
	// info.setNomemae(this.Filtra(rs.getString("SITUACAO") == null ? "" :
	// rs.getString("SITUACAO")));
	// filho.setLabel4("Situação:");
	// filho.setLabel5("");
	//
	// }
	// dtobito = this.Filtra(rs.getString("OBITO") == null ? "" :
	// rs.getString("OBITO"));
	//
	// dtobito = ObitoDao.findDataObito(mx, cpfcnpj, nome, conn);
	// info.setDtobito(dtobito);
	// info.setCpfcnpj(cpfcnpj);
	// filho.setInfotelefone(info);
	//
	//// filho.setTelefones(findTelefoneByCpf(cpfcnpj, qtdpesq, mx));
	//
	//// if (filho.isValid()) {
	//// this.filhos.add(filho);
	//// } else {
	//// continue;
	//// }
	//
	//
	// filho.setNumeroTelefone(this.Filtra(rs.getString("TELEFONE") == null ?
	// "-----------" : rs.getString("TELEFONE")));
	// filho.setEndereco(rs.getString("ENDERECO"));
	// filho.setNumero(rs.getString("NUMERO"));
	// filho.setComplemento(rs.getString("COMPLEMENTO"));
	// filho.setBairro(rs.getString("BAIRRO"));
	// filho.setCidade(rs.getString("CIDADE"));
	// filho.setUf(rs.getString("UF"));
	// filho.setCep(rs.getString("CEP"));
	// filho.setWhatsApp(rs.getString("WHATSAPP"));
	// filho.setOperadora(rs.getString("OPERADORA"));
	// filho.setGRPA_DS_PARENTESCO(rs.getString("GRPA_DS_PARENTESCO"));
	// filho.setCpfcnpj(rs.getString("CPFCNPJ"));
	// filho.setProcon(this.proconSP(filho.getNumeroTelefone(),conn));
	//
	//
	//
	// this.filhos.add(filho);
	// ind++;
	//
	// }
	// stmtN.close();
	// rs.close();
	//
	// this.paginaFilhosAnt = false;
	// this.paginaFilhosProx = false;
	// if (!(mx.getPaginaFilhos().equals(1)) && this.filhos.size() > 0) {
	// this.paginaFilhosAnt = true;
	// }
	// if (this.filhos.size() > Integer.valueOf(mx.getQtdpesq())) {
	// this.paginaFilhosProx = true;
	// this.filhos.remove(this.filhos.size() - 1);
	// }
	// } catch (SQLException e) {
	// mx.setResposta_consulta(true);
	// mx.setResposta_endereco(false);
	// mx.setResposta_endereco(false);
	// mx.setResposta_operadora(false);
	// mx.setResposta_nome(false);
	// mx.setResposta_cep(false);
	// mx.setResposta_veiculo(false);
	// mx.setResposta_historico_credito(false);
	// achou = false;
	// } finally {
	// if (requestFromViewSingleButton) {
	// setPossuiConexao(false);
	// }
	// releaseConnection();
	// }
	// if (ind > 0) {
	//
	// achou = true;
	//
	// } else {
	// mx.setResposta_consulta(true);
	// mx.setResposta_endereco(false);
	// mx.setResposta_endereco(false);
	// mx.setResposta_operadora(false);
	// mx.setResposta_nome(false);
	// mx.setResposta_cep(false);
	// mx.setResposta_veiculo(false);
	// mx.setResposta_historico_credito(false);
	// achou = false;
	// }
	// return achou;
	//
	// }

	public String GetSexo(String Nome, String CPF) throws SQLException {
		String sql = null;
		String Sexo = "-";
		Nome = Nome + " ";
		Nome = Nome.substring(0, Nome.indexOf(" "));
		java.sql.PreparedStatement stmtN = null;
		ResultSet rs = null;

		Boolean bAchou = false;

		try {
			sql = "select SEXO from info_complementares where cpfcnpj= ?";

			stmtN = this.connection.prepareStatement(sql);
			stmtN.setString(1, CPF);
			rs = stmtN.executeQuery();

			if (rs != null && rs.next()) {

				if (rs.getString("SEXO") != null) {
					Sexo = rs.getString("SEXO");
					bAchou = true;
				}

			}

			stmtN.close();
			stmtN = null;
			rs.close();
			rs = null;

			// if (Sexo.equals("-")){

			if (bAchou == false) {
				sql = "select TIPO from sexo where nome='" + Nome + "'";
				stmtN = this.connection.prepareStatement(sql);
				rs = stmtN.executeQuery();
				if (rs != null && rs.next()) {
					Sexo = rs.getString("TIPO");
				}
				if (Sexo.equals("")) {
					Sexo = "-";
				}
				// }
			}

			// Fetch each row from the result set
		} catch (SQLException e) {
			logger.error("Erro do metodo getSexo da classe Ressposta: " + e.getMessage());
			Sexo = "";
		} finally {
			if (rs != null && !rs.isClosed())
				rs.close();
			if (stmtN != null && !stmtN.isClosed())
				stmtN.close();
		}

		return Sexo;

	}

	public String Filtra(String StrText) {
		/*
		 * --------------------------------------------------------------------- -------
		 * Filtra uma string retirando caracteres inválidos.
		 */

		StrText = StrText.replace("Á", "A");
		StrText = StrText.replace("À", "A");
		StrText = StrText.replace("Â", "A");
		StrText = StrText.replace("Ã", "A");
		StrText = StrText.replace("Ä", "A");
		StrText = StrText.replace("É", "E");
		StrText = StrText.replace("È", "E");
		StrText = StrText.replace("Ê", "E");
		StrText = StrText.replace("Ë", "E");
		StrText = StrText.replace("Í", "I");
		StrText = StrText.replace("Ì", "I");
		StrText = StrText.replace("Î", "I");
		StrText = StrText.replace("Ï", "I");
		StrText = StrText.replace("Ó", "O");
		StrText = StrText.replace("Ò", "O");
		StrText = StrText.replace("Ô", "O");
		StrText = StrText.replace("Õ", "O");
		StrText = StrText.replace("Ö", "O");
		StrText = StrText.replace("Ú", "U");
		StrText = StrText.replace("Ù", "U");
		StrText = StrText.replace("Û", "U");
		StrText = StrText.replace("Ü", "U");
		StrText = StrText.replace("'", " ");
		StrText = StrText.replace("=", " ");
		StrText = StrText.replace("+", " ");
		StrText = StrText.replace("-", " ");
		StrText = StrText.replace("*", " ");
		StrText = StrText.replace("/", " ");
		StrText = StrText.replace(",", " ");
		StrText = StrText.replace(".", " ");
		StrText = StrText.replace(":", " ");
		StrText = StrText.replace(";", " ");
		StrText = StrText.replace("<", " ");
		StrText = StrText.replace(">", " ");
		StrText = StrText.replace("!", " ");
		StrText = StrText.replace("@", " ");
		StrText = StrText.replace("#", " ");
		StrText = StrText.replace("$", " ");
		StrText = StrText.replace("%", " ");
		StrText = StrText.replace("¨", " ");
		StrText = StrText.replace("&", " ");
		StrText = StrText.replace("(", " ");
		StrText = StrText.replace(")", " ");
		StrText = StrText.replace("[", " ");
		StrText = StrText.replace("]", " ");
		StrText = StrText.replace("{", " ");
		StrText = StrText.replace("}", " ");
		StrText = StrText.replace("?", " ");

		while (StrText.indexOf("  ") != -1) {
			StrText = StrText.replace("  ", " ");
		}

		return StrText;

	}

	public Boolean processaConsultaParentes(SqlToBind consulta, int tipopesquisa, LoginMBean mx) throws SQLException {

		/*
		 * Método de pesquisa parentes
		 *
		 * By SMarcio em 05/11/2013
		 */

		// Todo verificar erro na pesuisa com o cpf: 46334753800

		java.sql.Statement stmtN = null;
		ResultSet rs = null;
		int ind = 0;
		String aux = "";
		String anobase = "";
		int atualizacaoTel = 0;
		int atualizacaoBase = 0;
		String nome = "";
		String celular = "";
		String dtn = "";
		String dts = "";
		String ets = "";
		String areaterreno = "";
		String basecalc = "";
		String diavencimento = "";
		String fracaoideal = "";
		String daincl = "";
		String dalice = "";
		String sql = "";
		String sexo = "";
		String dtabertura = "";
		String cpfcnpj = "";
		String cpfcnpjant = "";
		Connection conn = this.getConnection();
		Boolean achou = false;
		Boolean requestFromViewSingleButton = false;
		if (!possuiConexao) {
			setPossuiConexao(true);
			requestFromViewSingleButton = true;
		}
		try {
			stmtN = conn.prepareStatement(consulta.getSql());

			for (int i = 0; i <= consulta.getBinds().size() - 1; i++)
				((PreparedStatement) stmtN).setString(i + 1, consulta.getBinds().get(i));

			rs = ((PreparedStatement) stmtN).executeQuery();

			// stmtN.setQueryTimeout(20);
			ind = 0;
			int id = 0;
			String nasc = "";
			String emails = "";
			Parentes t;
			String dtobito = "";
			Infocomplementares info;

			if (tipopesquisa == 1) {

				this.parentes = new ArrayList<Parentes>();

			}

			while (rs != null && rs.next()) {
				id++;
				t = new Parentes();
				info = new Infocomplementares();
				/*
				 * tipos de pesquisa 1 - pesquisa por telefone 2 - parentes
				 *
				 */

				/* Pesquisa CpfCnpj ConfirmeOnLine New */

				cpfcnpj = this.Filtra(rs.getString("CPFCNPJ") == null ? "" : rs.getString("CPFCNPJ"));
				List<String> list = getQsaEmpresasInfo(cpfcnpj);

				if (!cpfcnpj.equals(cpfcnpjant)) {

					t.setEoprimeiro(true);
					cpfcnpjant = cpfcnpj;
					id = 1;
				}
				nome = this.Filtra(rs.getString("NOME") == null ? "" : rs.getString("NOME"));

				if (nome.equals("")) {

					nome = this.Filtra(rs.getString("PROPRIETARIO") == null ? "" : rs.getString("PROPRIETARIO"));

				}

				info.setNome(nome);

				sexo = this.Filtra(GetSexo(rs.getString("PROPRIETARIO") == null ? "" : rs.getString("PROPRIETARIO"),
						rs.getString("CPFCNPJ") == null ? "" : rs.getString("CPFCNPJ")));

				if (sexo.equals("M")) {

					sexo = "MASCULINO";

				}
				if (sexo.equals("F")) {

					sexo = "FEMININO";

				}
				String birthday = dao.findBirthday(cpfcnpj);
				Integer idade = Utils.calculaIdade(birthday);
				// this.infocomplementares.setDtnasc(rs.getString("NASC") ==
				// null ? "" : rs.getString("NASC")+idade);
				info.setDtnasc(birthday != null ? birthday + " - " + idade + " anos." : null);

				if (cpfcnpj.length() < 14) {
					t.setLabel1("Dt. Nascimento:");

					info.setSigno(Utils.findSigno(info.getDtnasc(), "dd/MM/yyyy"));
					t.setLabel2("Signo:");

					info.setSexo(sexo);
					t.setLabel3("Sexo:");

					info.setNomemae(this.Filtra(rs.getString("MAE") == null ? "" : rs.getString("MAE")));
					t.setLabel4("Nome da Mãe:");

					info.setTituloEleitor(this.getTituloEleitoral(cpfcnpj, this.getConnection()));
					t.setLabel5("Título de Eleitor:");

					// Rodrigo Almeida - 21/07/2017
					t.setLabel6("Grau de Parentesco:");
					info.setGrpa_ds_parentesco(
							rs.getString("GRPA_DS_PARENTESCO") == null ? "" : rs.getString("GRPA_DS_PARENTESCO"));

				}

				if (cpfcnpj.length() == 14) {
					t.setLabel1("Dt. Fundação:");

					info.setSigno(this.Filtra(list.get(2) == null ? "" : list.get(2)));
					t.setLabel2("Nome Fantasia:");

					info.setSexo(this.Filtra(list.get(3) == null ? "" : list.get(3)));
					t.setLabel3("Natureza:");

					info.setNomemae(this.Filtra(list.get(1) == null ? "" : list.get(1)));
					t.setLabel4("Situação:");
					t.setLabel5("");

				}
				dtobito = this.Filtra(rs.getString("OBITO") == null ? "" : rs.getString("OBITO"));

				dtobito = ObitoDao.findDataObito(mx, cpfcnpj, nome, conn);
				info.setDtobito(dtobito);
				info.setCpfcnpj(this.Filtra(rs.getString("CPFCNPJ") == null ? "" : rs.getString("CPFCNPJ")));
				t.setInfotelefone(info);

				t.setId(id);
				t.setProprietario(
						this.Filtra(rs.getString("PROPRIETARIO") == null ? "" : rs.getString("PROPRIETARIO")));
				t.setEndereco(this.Filtra(rs.getString("ENDERECO") == null ? "" : rs.getString("ENDERECO")));
				t.setNumero(this.Filtra(rs.getString("NUMERO") == null ? "" : rs.getString("NUMERO")));
				t.setComplemento(this.Filtra(rs.getString("COMPLEMENTO") == null ? "" : rs.getString("COMPLEMENTO")));
				t.setBairro(this.Filtra(rs.getString("BAIRRO") == null ? "" : rs.getString("BAIRRO")));
				t.setCidade(this.Filtra(rs.getString("CIDADE") == null ? "" : rs.getString("CIDADE")));
				t.setCep(this.Filtra(rs.getString("CEP") == null ? "" : rs.getString("CEP")));
				t.setUf(this.Filtra(rs.getString("UF") == null ? "" : rs.getString("UF")));
				t.setCpfcnpj(cpfcnpj);
				t.setNumeroTelefone(
						this.Filtra(rs.getString("TELEFONE") == null ? "-----------" : rs.getString("TELEFONE")));

				t.setStatusLinha(this.Filtra(rs.getString("STATUS_LINHA") == null ? "" : rs.getString("STATUS_LINHA")));
				t.setAtual(this.Filtra(rs.getString("ATUAL") == null ? "" : rs.getString("ATUAL")));

				/*
				 * Verifica se o cliente possui direito a base que ele esta consultando, se não
				 * mostra alguns dados mas os endereços e telefones não mostra
				 */

				if (Conexao.GetSerial(conn, mx.getUsuario().getLogin()).indexOf(t.getUf()) != -1) {

					t.setNumeroTelefone(
							this.Filtra(rs.getString("TELEFONE") == null ? "-----------" : rs.getString("TELEFONE")));
					t.setEndereco(this.Filtra(rs.getString("ENDERECO") == null ? "" : rs.getString("ENDERECO")));
					t.setNumero(this.Filtra(rs.getString("NUMERO") == null ? "" : rs.getString("NUMERO")));
					t.setComplemento(
							this.Filtra(rs.getString("COMPLEMENTO") == null ? "" : rs.getString("COMPLEMENTO")));
					t.setBairro(this.Filtra(rs.getString("BAIRRO") == null ? "" : rs.getString("BAIRRO")));
					t.setCidade(this.Filtra(rs.getString("CIDADE") == null ? "" : rs.getString("CIDADE")));
					t.setCep(this.Filtra(rs.getString("CEP") == null ? "" : rs.getString("CEP")));
					t.setProcon(this.proconSP(t.getNumeroTelefone(), conn));
					if (t.getNumeroTelefone().length() < 9) {

						t.setNumeroTelefone("-----------");

					}

				} else {

					t.setNumeroTelefone("--Estado não contratado.");
					t.setEndereco("Estado não contratado ");
					t.setNumero("-");
					t.setComplemento("-");
					t.setBairro("-");
					t.setCidade("-");
					t.setCep("-");
					t.setProcon("-");

				}

				t.setRatingTelefone(t.getAtual(), getUseStatus());
				t.setWhatsApp(rs.getString("WHATSAPP"));
				this.parentes.add(t);

				ind++;

			}

			this.setPaginaparentesAnt(false);
			this.setPaginaparentesProx(false);
			if (!(mx.getPaginaParentes().equals(1)) && this.parentes.size() > 0) {
				this.setPaginaparentesAnt(true);
			}
			if (this.parentes.size() > Integer.valueOf(mx.getQtdpesq())) {
				this.setPaginaparentesProx(true);
				this.parentes.remove(this.parentes.size() - 1);
			}

		}
		// catch(SQLTimeoutException e){
		// processaConsultaParentes(Consulta, tipopesquisa, mx);
		// }
		catch (SQLException e) {
			logger.error("Erro do metodo processaConsultaParentes da clase resposta: " + e.getMessage());
			achou = false;
		} finally {
			if (requestFromViewSingleButton) {
				setPossuiConexao(false);
			}

			if (rs != null && !rs.isClosed())
				rs.close();
			if (stmtN != null && !stmtN.isClosed())
				stmtN.close();
			releaseConnection();
		}
		if (ind > 0) {
			achou = true;
		} else {
			achou = false;
		}
		return achou;

	}

	public Boolean pesquisa_parentes(LoginMBean mb, Integer comando) {

		/*
		 * Método de pesquisa parentes
		 *
		 *
		 * By SMarcio em 08/10/2013
		 */

		SqlToBind sql = new SqlToBind();
		Boolean ok = false;
		Integer paginaFinal = 0;
		Integer paginaInicial = 0;
		Integer qtdpesq = 0;
		Integer pagina = 0;
		mb.setResposta_consulta(true);
		mb.setResposta_conArmazenada(false);
		mb.setResposta_endereco(false);
		mb.setResposta_nome(false);
		mb.setResposta_razao(false);
		mb.setResposta_cep(false);
		mb.setResposta_veiculo(false);
		mb.setResposta_historico_credito(false);
		try {

			qtdpesq = Integer.parseInt(mb.getQtdpesq());
			pagina = mb.getPaginaParentes();

			if (qtdpesq < 1) {

				qtdpesq = 1;

			}
			if (qtdpesq > 100) {

				qtdpesq = 100;
			}
			/* Avança registro */

			/*
			 * Se o comando 0 for passado, sempre vai iniciar a pesquisa do inicio
			 */

			if (comando == 1) {

				pagina = pagina + 1;

			}

			/* Volta registro */
			if (comando == 2) {

				pagina = pagina - 1;
			}

			paginaFinal = qtdpesq * pagina;
			paginaInicial = (paginaFinal - qtdpesq) + 1;

			if (pagina == 1 || pagina < 1) {

				paginaFinal = qtdpesq;
				paginaInicial = 1;
				pagina = 1;
			}

			/* 250 e hardcode e o maximo de registros que pesquisamos */
			if (pagina > (250 / qtdpesq)) {

				pagina = 250 / qtdpesq;
				paginaFinal = 250;
				paginaInicial = (paginaFinal - qtdpesq) + 1;
			}

			if (comando == 0) {

				pagina = 1;
				paginaFinal = qtdpesq;
				paginaInicial = 1;

			}
			paginaFinal += 1;

			mb.setPaginaParentes(pagina);

			sql = processapesquisa_parentes(this.getInfocomplementares().getNomemae(),
					this.getInfocomplementares().getCpfcnpj(),
					this.getInfocomplementares().getCpfcnpj().substring(8, 9),
					br.com.confirmeonline.util.SQLConstantes.QTD_MAX_PESQUISA, paginaInicial, paginaFinal);

			// sql.append(" ORDER BY CPFCNPJ,NUMERO, COMPLEMENTO,
			// PROPRIETARIO");

			// sql.append(" ) ORDER BY NUMERO, COMPLEMENTO, PROPRIETARIO )
			// PAGINA WHERE ( ROWNUM <= "+Util.SQLConstantes.QTD_MAX_PESQUISA+"
			// ) ) WHERE ( PAGINA_RN >= '"+ paginaInicial + "' AND PAGINA_RN <=
			// '"+ paginaFinal + "' ) ");
			// sql.append(" ORDER BY CPFCNPJ,NUMERO, COMPLEMENTO,
			// PROPRIETARIO");

			/*
			 * Aciona o método processaConsulta Que é responsável por ir ao banco de dados,
			 * ler as informações, e coloca-las dentro do managedBean By SMarcio em
			 * 30/09/2013
			 */

			ok = processaConsultaParentes(sql, 1, mb);
			return ok;

		} catch (Exception e) {

			return false;

		}
	}

	private SqlToBind processapesquisa_parentes(String nomemae, String cpfcnpj, String substring, int qtdMaxPesquisa,
			Integer paginaInicial, Integer paginaFinal) {

		StringBuilder sql = new StringBuilder();
		SqlToBind resultado = new SqlToBind();

		sql.append(" SELECT * FROM ( ");
		sql.append(" SELECT PAGINA.*,ROWNUM PAGINA_RN FROM ( ");
		sql.append(" SELECT * FROM ( ");
		sql.append(
				" SELECT (SELECT STATUS FROM TELEFONES_STATUS WHERE TELEFONE = T.TELEFONE AND ROWNUM <= 1) AS STATUS_LINHA,");
		sql.append(
				" (CASE WHEN (SELECT FLAG FROM TBL_WHATSAPP T2 WHERE T2.TELEFONE = T.TELEFONE AND ROWNUM <= 1) = 1 THEN 1 ELSE 0 END) AS WHATSAPP, ");
		sql.append(" (SELECT IS_DATE_CONFIRME(DT_OBITO) FROM OBITO WHERE NU_CPF = CPFCGC AND ROWNUM<=1 ) AS OBITO,");
		sql.append(" (SELECT DT_ABERTURA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS DT_ABERTURA,");
		sql.append(" (SELECT SITUACAO FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS SITUACAO,");
		sql.append(" (SELECT FANTASIA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS FANTASIA,");
		sql.append(" (SELECT DESC_NATUREZA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS NATUREZA,");
		sql.append(
				" I.NOME,T.PROPRIETARIO AS PROPRIETARIO,T.ATUAL,T.TELEFONE,T.ENDERECO AS ENDERECO,T.NUMERO AS NUMERO,T.COMPLEMENTO,T.BAIRRO,T.CEP,T.CIDADE,T.UF,I.CPFCNPJ AS CPFCNPJ,");
		sql.append(
				" i.CPF_CONJUGE AS CPF_CONJUGE, PEGA_NOME_INFO_COMP(I.CPF_CONJUGE) AS NOME_CONJUGE,I.NOME_MAE AS MAE,TO_CHAR(I.DATA_NASC,'DD/MM/YYYY') AS NASC,' ' AS TITULO,T.OPERADORA,TO_CHAR(T.DT_INSTALACAO,'YYYY-MM-DD') AS DT_INSTALACAO, I.SIGNO AS SIGNO ");
		sql.append(" FROM TELEFONES T,INFO_COMPLEMENTARES I,FINAN.CRED_MEGA_CEP M ");
		sql.append(
				" WHERE  I.NOME = ? AND I.CPFCNPJ <> ? AND SUBSTR(I.CPFCNPJ,9,1)= ? AND I.CPFCNPJ = T.CPFCGC(+) AND T.CEP = M.CEP(+) ");
		sql.append(" UNION ALL ");
		sql.append(
				" SELECT (SELECT STATUS FROM TELEFONES_STATUS WHERE TELEFONE = T.TELEFONE AND ROWNUM <= 1) AS STATUS_LINHA,");
		sql.append(
				" (CASE WHEN (SELECT FLAG FROM TBL_WHATSAPP T2 WHERE T2.TELEFONE = T.TELEFONE AND ROWNUM <= 1) = 1 THEN 1 ELSE 0 END) AS WHATSAPP, ");
		sql.append(" (SELECT IS_DATE_CONFIRME(DT_OBITO) FROM OBITO WHERE NU_CPF = CPFCGC AND ROWNUM<=1 ) AS OBITO,");
		sql.append(" (SELECT DT_ABERTURA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS DT_ABERTURA,");
		sql.append(" (SELECT SITUACAO FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS SITUACAO,");
		sql.append(" (SELECT FANTASIA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS FANTASIA,");
		sql.append(" (SELECT DESC_NATUREZA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS NATUREZA,");
		sql.append(
				" I.NOME,T.PROPRIETARIO AS PROPRIETARIO,T.ATUAL,T.TELEFONE,T.ENDERECO AS ENDERECO,T.NUMERO AS NUMERO,T.COMPLEMENTO,T.BAIRRO,T.CEP,T.CIDADE,T.UF,I.CPFCNPJ AS CPFCNPJ,");
		sql.append(
				" i.CPF_CONJUGE AS CPF_CONJUGE, PEGA_NOME_INFO_COMP(I.CPF_CONJUGE) AS NOME_CONJUGE,I.NOME_MAE AS MAE,TO_CHAR(I.DATA_NASC,'DD/MM/YYYY') AS NASC,' ' AS TITULO,T.OPERADORA,TO_CHAR(T.DT_INSTALACAO,'YYYY-MM-DD') AS DT_INSTALACAO, I.SIGNO AS SIGNO ");
		sql.append(" FROM TELEFONES T,INFO_COMPLEMENTARES I,FINAN.CRED_MEGA_CEP M ");
		sql.append(
				" WHERE I.NOME_MAE = ? AND I.CPFCNPJ <> ? AND SUBSTR(I.CPFCNPJ,9,1)= ? AND I.CPFCNPJ = T.CPFCGC(+) AND T.CEP = M.CEP(+) ");
		sql.append(" )  ) PAGINA  WHERE ( ROWNUM <= ? ) ) WHERE ( PAGINA_RN >= ? AND PAGINA_RN <= ? ) ");

		resultado.limpaLista();
		resultado.addString(nomemae);
		resultado.addString(cpfcnpj);
		resultado.addString(substring);
		resultado.addString(nomemae);
		resultado.addString(cpfcnpj);
		resultado.addString(substring);
		resultado.addString(String.valueOf(qtdMaxPesquisa));
		resultado.addString(String.valueOf(paginaInicial));
		resultado.addString(String.valueOf(paginaFinal));

		resultado.setSql(sql.toString());

		return resultado;

	}

	public Boolean processaConsultaVeiculos(String Consulta, int tipopesquisa, LoginMBean mx) {

		/*
		 * Método de pesquisa Veiculos
		 *
		 * By SMarcio em 05/11/2013
		 */

		java.sql.Statement stmtN;
		ResultSet rs;
		int ind = 0;
		String aux = "";
		String anobase = "";
		int atualizacaoTel = 0;
		int atualizacaoBase = 0;
		String nome = "";
		String celular = "";
		String dtn = "";
		String dts = "";
		String ets = "";
		String areaterreno = "";
		String basecalc = "";
		String diavencimento = "";
		String fracaoideal = "";
		String daincl = "";
		String dalice = "";
		String sql = "";
		String sexo = "";
		String dtabertura = "";
		String cpfcnpj = "";
		String cpfcnpjant = "";
		Boolean achou = false;
		Boolean requestFromViewSingleButton = false;
		try {

			stmtN = this.getConnection().createStatement();
			if (!possuiConexao) {
				setPossuiConexao(true);
				requestFromViewSingleButton = true;
			}
			rs = stmtN.executeQuery(Consulta);
			ind = 0;
			int id = 0;
			String nasc = "";
			String emails = "";
			Veiculo v;
			String dtobito = "";

			if (tipopesquisa == 1) {

				this.veiculos = new ArrayList<Veiculo>();

			}

			while (rs != null && rs.next()) {

				// Rodrigo Almeida 28/10/2019
//                Verificando se um determinado CPF está relacionado na tabela TB_PROTECAO_DADOS_PESSOAIS
				protecaoDados = false;
				protecaoDados = verificaProtecaoDadosPessoais(rs.getString("CPFCNPJ"), "");

				if (!protecaoDados) {

					id++;
					v = new Veiculo();
					/*
					 * Query: V.PLACA V.MARCA V.RENAVAN V.ANOFAB V.CHASSI V.COMBU V.ANOMODE V.PROPRI
					 * V.END V.NUM V.COMPL V.BAIRRO V.CEP V.CPF AS CPFCNPJ V.CIDADE V.ESTADO
					 * V.DAINCL V.DALICE
					 */

					/* Pesquisa CpfCnpj ConfirmeOnLine New */

					cpfcnpj = this.Filtra(rs.getString("CPFCNPJ") == null ? "" : rs.getString("CPFCNPJ"));

					if (!cpfcnpj.equals(cpfcnpjant)) {

						v.setEoprimeiro(true);
						cpfcnpjant = cpfcnpj;
						id = 1;

					}

					v.setAnofab(rs.getString("ANOFAB") == null ? "" : rs.getString("ANOFAB"));
					v.setAnomode(rs.getString("ANOMODE") == null ? "" : rs.getString("ANOMODE"));
					v.setChassi(rs.getString("CHASSI") == null ? "" : rs.getString("CHASSI"));
					v.setCombu(rs.getString("COMBU") == null ? "" : rs.getString("COMBU"));
					v.setDaincl(rs.getString("DAINCL") == null ? "" : rs.getString("DAINCL"));
					v.setDalice(rs.getString("DALICE") == null ? "" : rs.getString("DALICE"));
					v.setMarca(rs.getString("MARCA") == null ? "" : rs.getString("MARCA"));
					v.setChassi(rs.getString("CHASSI") == null ? "" : rs.getString("CHASSI"));
					v.setPlaca(rs.getString("PLACA") == null ? "" : rs.getString("PLACA"));
					v.setRenavam(rs.getString("RENAVAN") == null ? "" : rs.getString("RENAVAN"));
					v.setCpfcnpj(cpfcnpj);
					v.setNome(this.Filtra(rs.getString("PROPRI") == null ? "" : rs.getString("PROPRI")));
					v.setEnd(this.Filtra(rs.getString("END") == null ? "" : rs.getString("END")));
					v.setNumero(this.Filtra(rs.getString("NUM") == null ? "" : rs.getString("NUM")));
					v.setComplemento(this.Filtra(rs.getString("COMPL") == null ? "" : rs.getString("COMPL")));
					v.setBairro(this.Filtra(rs.getString("BAIRRO") == null ? "" : rs.getString("BAIRRO")));
					v.setCidade(this.Filtra(rs.getString("CIDADE") == null ? "" : rs.getString("CIDADE")));
					v.setEstado(this.Filtra(rs.getString("ESTADO") == null ? "" : rs.getString("ESTADO")));
					v.setCep(this.Filtra(rs.getString("CEP") == null ? "" : rs.getString("CEP")));
					this.veiculos.add(v);

					ind++;
				}

			}
			if (rs != null && !rs.isClosed())
				rs.close();
			if (stmtN != null && !stmtN.isClosed())
				stmtN.close();

			/* verifica se vai ser necessário colocar paginacao na tela */

			// if ( mx.getPaginaVeiculos() == 1 ){
			//
			// if ( ind < Integer.parseInt(mx.getQtdpesq()) ){
			//
			// this.setPaginaveiculos(false);
			//
			//
			// }else{
			//
			// this.setPaginaveiculos(true);
			//
			// }
			//
			// }
			this.setPaginaveiculosAnt(false);
			this.setPaginaveiculosProx(false);
			if (!(mx.getPaginaVeiculos().equals(1)) && this.veiculos.size() > 0) {
				this.setPaginaveiculosAnt(true);
			}
			if (this.veiculos.size() > Integer.valueOf(mx.getQtdpesq())) {
				this.setPaginaveiculosProx(true);
				this.veiculos.remove(this.veiculos.size() - 1);
			}

		} catch (SQLException e) {
			logger.error("Erro no metodo processaConsultaVeiculos da classe Resposta: " + e.getMessage());
			achou = false;
		} finally {
			if (requestFromViewSingleButton) {
				setPossuiConexao(false);
			}
			releaseConnection();
		}
		if (ind > 0) {
			achou = true;
		} else {
			achou = false;
		}
		return achou;

	}

	public Boolean processaConsultaMoradores(SqlToBind consulta, int tipopesquisa, LoginMBean mx, Integer regra,
			Telefone tel, Integer paginaInicial, Integer paginaFinal) throws SQLException {

		/*
		 * Método de pesquisa Moradores
		 *
		 * By SMarcio em 05/11/2013
		 */
		CallableStatement stmt = null;
		java.sql.Statement stmtN = null;
		ResultSet rs = null;
		int ind = 0;
		String aux = "";
		String anobase = "";
		int atualizacaoTel = 0;
		int atualizacaoBase = 0;
		String nome = "";
		String celular = "";
		String dtn = "";
		String dts = "";
		String ets = "";
		String areaterreno = "";
		String basecalc = "";
		String diavencimento = "";
		String fracaoideal = "";
		String daincl = "";
		String dalice = "";
		String sql = "";
		String sexo = "";
		String dtabertura = "";
		String cpfcnpj = "";
		String cpfcnpjant = "";
		Boolean achou = false;
		int id = 0;
		String complemento;
		String logradouro;
		Connection conn = this.getConnection();
		Boolean requestFromViewSingleButton = false;
		if (!possuiConexao) {
			setPossuiConexao(true);
			requestFromViewSingleButton = true;
		}
		try {
			complemento = tel.getComplemento();
			logradouro = tel.getEndereco();

			int i = 1;
			// conn=((DelegatingConnection)conn).getInnermostDelegate();

			stmt = conn.prepareCall("BEGIN CONFIRME_PESQUISA_MORADORES2(?,?,?,?,?,?,?,?,?); END;");

			stmt.setString(i++, tel.getCpfcnpj());
			stmt.setString(i++, tel.getNumero());
			stmt.setString(i++, tel.getEndereco());
			stmt.setString(i++, tel.getComplemento());
			stmt.setString(i++, tel.getCep());
			stmt.setInt(i++, paginaInicial);
			stmt.setInt(i++, paginaFinal);
			stmt.setInt(i++, br.com.confirmeonline.util.SQLConstantes.QTD_MAX_PESQUISA);
			stmt.registerOutParameter(i++, OracleTypes.CURSOR);
			stmt.execute();

			// stmtN = conn.createStatement();
			// rs = stmtN.executeQuery(Consulta);
			ind = 0;
			String nasc = "";
			String emails = "";
			Moradores t;
			String dtobito = "";
			Infocomplementares info;
			String ENDERECOBASE = "";
			String COMPLEMENTOBASE = "";
			Boolean morador = false;

			if (tipopesquisa == 1) {

				this.moradores = new ArrayList<Moradores>();

			}

			while (rs != null && rs.next()) {
				/*
				 * tipos de pesquisa 1 - pesquisa por telefone 2 - parentes
				 *
				 */

				/* Pesquisa CpfCnpj ConfirmeOnLine New */

				cpfcnpj = this.Filtra(rs.getString("CPFCNPJ") == null ? "" : rs.getString("CPFCNPJ"));
				List<String> list = getQsaEmpresasInfo(cpfcnpj);
				ENDERECOBASE = rs.getString("ENDERECO");
				if (rs.getString("COMPLEMENTO") == null) {

					COMPLEMENTOBASE = "";
					regra = 3;

				} else {

					COMPLEMENTOBASE = rs.getString("COMPLEMENTO");

				}

				morador = true;
				// morador = validaMorador(complemento,
				// COMPLEMENTOBASE,ENDERECOBASE, logradouro, regra);

				if (rs.getString("PROPRIETARIO") != null && morador == true) {

					id++;
					t = new Moradores();
					info = new Infocomplementares();

					if (!cpfcnpj.equals(cpfcnpjant)) {

						t.setEoprimeiro(true);
						cpfcnpjant = cpfcnpj;
						id = 1;
					}
					nome = this.Filtra(rs.getString("NOME") == null ? "" : rs.getString("NOME"));

					if (nome.equals("")) {

						nome = this.Filtra(rs.getString("PROPRIETARIO") == null ? "" : rs.getString("PROPRIETARIO"));

					}

					info.setNome(nome);

					sexo = this.Filtra(GetSexo(rs.getString("PROPRIETARIO") == null ? "" : rs.getString("PROPRIETARIO"),
							rs.getString("CPFCNPJ") == null ? "" : rs.getString("CPFCNPJ")));

					if (sexo.equals("M")) {

						sexo = "MASCULINO";

					}
					if (sexo.equals("F")) {

						sexo = "FEMININO";

					}
					String birthday = dao.findBirthday(cpfcnpj);
					Integer idade = Utils.calculaIdade(birthday);
					// this.infocomplementares.setDtnasc(rs.getString("NASC") ==
					// null ? "" : rs.getString("NASC")+idade);
					info.setDtnasc(birthday != null ? birthday + " - " + idade + " anos." : null);

					if (cpfcnpj.length() < 14) {
						t.setLabel1("Dt. Nascimento:");

						info.setSigno(Utils.findSigno(info.getDtnasc(), "dd/MM/yyyy"));
						t.setLabel2("Signo:");

						info.setSexo(sexo);
						t.setLabel3("Sexo:");

						info.setNomemae(this.Filtra(rs.getString("MAE") == null ? "" : rs.getString("MAE")));
						t.setLabel4("Nome da Mãe:");

						info.setTituloEleitor(this.getTituloEleitoral(cpfcnpj, this.getConnection()));
						t.setLabel5("Título de Eleitor:");
					}

					if (cpfcnpj.length() == 14) {
						t.setLabel1("Dt. Fundação:");

						info.setSigno(this.Filtra(list.get(2) == null ? "" : list.get(2)));
						t.setLabel2("Nome Fantasia:");

						info.setSexo(this.Filtra(list.get(3) == null ? "" : list.get(3)));
						t.setLabel3("Natureza:");

						info.setNomemae(this.Filtra(list.get(1) == null ? "" : list.get(1)));
						t.setLabel4("Situação:");
						t.setLabel5("");

					}
					dtobito = this.Filtra(rs.getString("OBITO") == null ? "" : rs.getString("OBITO"));

					dtobito = ObitoDao.findDataObito(mx, cpfcnpj, nome, conn);
					info.setDtobito(dtobito);
					info.setCpfcnpj(this.Filtra(rs.getString("CPFCNPJ") == null ? "" : rs.getString("CPFCNPJ")));
					t.setInfotelefone(info);

					t.setId(id);
					t.setProprietario(
							this.Filtra(rs.getString("PROPRIETARIO") == null ? "" : rs.getString("PROPRIETARIO")));
					t.setEndereco(this.Filtra(rs.getString("ENDERECO") == null ? "" : rs.getString("ENDERECO")));
					t.setNumero(this.Filtra(rs.getString("NUMERO") == null ? "" : rs.getString("NUMERO")));
					t.setComplemento(
							this.Filtra(rs.getString("COMPLEMENTO") == null ? "" : rs.getString("COMPLEMENTO")));
					t.setBairro(this.Filtra(rs.getString("BAIRRO") == null ? "" : rs.getString("BAIRRO")));
					t.setCidade(this.Filtra(rs.getString("CIDADE") == null ? "" : rs.getString("CIDADE")));
					t.setCep(this.Filtra(rs.getString("CEP") == null ? "" : rs.getString("CEP")));
					t.setUf(this.Filtra(rs.getString("UF") == null ? "" : rs.getString("UF")));
					t.setCpfcnpj(cpfcnpj);
					t.setNumeroTelefone(
							this.Filtra(rs.getString("TELEFONE") == null ? "-----------" : rs.getString("TELEFONE")));

					t.setStatusLinha(
							this.Filtra(rs.getString("STATUS_LINHA") == null ? "" : rs.getString("STATUS_LINHA")));
					t.setAtual(this.Filtra(rs.getString("ATUAL") == null ? "" : rs.getString("ATUAL")));
					/*
					 * Verifica se o cliente possui direito a base que ele esta consultando, se não
					 * mostra alguns dados mas os endereços e telefones não mostra
					 */

					if (Conexao.GetSerial(conn, mx.getUsuario().getLogin()).indexOf(t.getUf()) != -1) {

						t.setNumeroTelefone(this
								.Filtra(rs.getString("TELEFONE") == null ? "-----------" : rs.getString("TELEFONE")));
						t.setEndereco(this.Filtra(rs.getString("ENDERECO") == null ? "" : rs.getString("ENDERECO")));
						t.setNumero(this.Filtra(rs.getString("NUMERO") == null ? "" : rs.getString("NUMERO")));
						t.setComplemento(
								this.Filtra(rs.getString("COMPLEMENTO") == null ? "" : rs.getString("COMPLEMENTO")));
						t.setBairro(this.Filtra(rs.getString("BAIRRO") == null ? "" : rs.getString("BAIRRO")));
						t.setCidade(this.Filtra(rs.getString("CIDADE") == null ? "" : rs.getString("CIDADE")));
						t.setCep(this.Filtra(rs.getString("CEP") == null ? "" : rs.getString("CEP")));
						t.setProcon(this.proconSP(t.getNumeroTelefone(), conn));
						if (t.getNumeroTelefone().length() < 9) {

							t.setNumeroTelefone("-----------");

						}

					} else {

						t.setNumeroTelefone("--Estado não contratado.");
						t.setEndereco("Estado não contratado ");
						t.setNumero("-");
						t.setComplemento("-");
						t.setBairro("-");
						t.setCidade("-");
						t.setCep("-");
						t.setProcon("-");

					}
					t.setRatingTelefone(t.getAtual(), getUseStatus());
					t.setWhatsApp(rs.getString("WHATSAPP"));
					this.moradores.add(t);

					ind++;

				}

			}

			/* verifica se vai ser necessário colocar paginacao na tela */

			// if ( ind <Integer.parseInt(mx.getQtdpesq()) ){
			//
			// this.setPaginamoradores(false);
			//
			//
			// }else{
			//
			// this.setPaginamoradores(true);
			//
			// }

			this.setPaginamoradoresAnt(false);
			this.setPaginamoradoresProx(false);
			if (!(mx.getPaginaMoradores().equals(1)) && this.moradores.size() > 0) {
				this.setPaginamoradoresAnt(true);
			}
			if (this.moradores.size() > Integer.valueOf(mx.getQtdpesq())) {
				this.setPaginamoradoresProx(true);
				this.moradores.remove(this.moradores.size() - 1);
			}

		} catch (Exception e) {
			logger.error("Erro no metodo processaConsultaMoradores da classe Resposta: " + e.getMessage());
			achou = false;
		} finally {
			if (requestFromViewSingleButton) {
				setPossuiConexao(false);
			}
			releaseConnection();

			if (rs != null && !rs.isClosed())
				rs.close();
			if (stmt != null && !stmt.isClosed())
				stmt.close();
		}

		if (ind > 0) {
			achou = true;
		} else {
			achou = false;
		}
		return achou;

	}

	public boolean validaMorador(String complementoMorador, String complementoBase, String enderecobase,
			String logradouro, int regra) {

		/*
		 * Valida se a pessoa que foi recuprada da base de dados e um morador do mesmo
		 * numero
		 */

		boolean resposta = false;
		String enderecoPesq = null;

		enderecoPesq = logradouro.replaceAll("@", " ");

		try {

			if (complementoBase != null) {

				complementoMorador = complementoMorador.trim();
				complementoBase = complementoBase.trim();

				if (regra == 1) {

					if (complementoMorador.length() > 0) {

						complementoMorador = pegaNumero(complementoMorador);
						complementoBase = pegaNumero(complementoBase);
					}

					if (complementoMorador.equals(complementoBase)) {

						resposta = true;

					}

				} else if (regra == 2) {

					complementoMorador = pegaNumero(complementoMorador);
					complementoBase = pegaNumero(complementoBase);

					if (complementoMorador.equals(complementoBase)) {

						resposta = true;

					}

				} else if (regra == 3) {

					if (enderecoPesq.contains(enderecobase) && complementoMorador == null) {

						resposta = true;

					}

				}

			}
		} catch (Exception e) {

			resposta = false;

		}

		return resposta;

	}

	public String pegaNumero(String str) {

		String resposta = "";
		String auxiliar = "";

		try {

			for (int i = str.length(); i >= 0; --i) {

				auxiliar = str.substring(i - 1, i);
				for (int x = 0; x <= 9; x++) {

					try {
						if (Integer.parseInt(auxiliar) == x) {

							resposta = auxiliar + resposta;
							break;

						}
					} catch (Exception e) {

						// Peguei uma string vamos em frente
						break;

					}

				}

			}

		} catch (Exception e) {

			// Deu erro não retornarei nada

		}
		return resposta;

	}

	public Boolean paginacao_moradores(LoginMBean mb, Integer comando, Integer indice) {

		/*
		 * Método paginacao Moradores
		 *
		 *
		 * By SMarcio em 08/10/2013
		 */

		StringBuilder sql = new StringBuilder();
		Boolean ok = false;
		Integer paginaFinal = 0;
		Integer paginaInicial = 0;
		Integer qtdpesq = 0;
		Integer pagina = 0;
		mb.setResposta_consulta(true);
		mb.setResposta_conArmazenada(false);
		mb.setResposta_endereco(false);
		mb.setResposta_nome(false);
		mb.setResposta_razao(false);
		mb.setResposta_cep(false);
		mb.setResposta_veiculo(false);
		mb.setResposta_historico_credito(false);

		try {

			qtdpesq = Integer.parseInt(mb.getQtdpesq());
			pagina = mb.getPaginaMoradores();

			if (qtdpesq < 1) {

				qtdpesq = 1;

			}
			if (qtdpesq > 100) {

				qtdpesq = 100;
			}
			/* Avança registro */

			/*
			 * Se o comando 0 for passado, sempre vai iniciar a pesquisa do inicio
			 */

			if (comando == 1) {

				pagina = pagina + 1;

			}

			/* Volta registro */
			if (comando == 2) {

				pagina = pagina - 1;
			}

			paginaFinal = qtdpesq * pagina;
			paginaInicial = (paginaFinal - qtdpesq) + 1;

			if (pagina == 1 || pagina < 1) {

				paginaFinal = qtdpesq;
				paginaInicial = 1;
				pagina = 1;
			}

			/* 250 e hardcode e o maximo de registros que pesquisamos */
			if (pagina > (250 / qtdpesq)) {

				pagina = 250 / qtdpesq;
				paginaFinal = 250;
				paginaInicial = (paginaFinal - qtdpesq) + 1;
			}

			if (comando == 0) {

				pagina = 1;
				paginaFinal = qtdpesq;
				paginaInicial = 1;

			}
			paginaFinal += 1;

			mb.setPaginaMoradores(pagina);
			Integer regra = 1;

			if (this.getTelefone().get(indice).getComplemento().length() > 0) {

				if (this.getTelefone().get(indice).getComplemento().substring(0, 1).equals("B")) {

					regra = 1;

				} else {

					regra = 2;
				}

			}

			sql.append(" SELECT * FROM ( ");
			sql.append(" SELECT PAGINA.*,ROWNUM PAGINA_RN FROM ( ");
			sql.append(" SELECT * FROM ( ");
			sql.append(
					" SELECT (SELECT STATUS FROM TELEFONES_STATUS WHERE TELEFONE = T.TELEFONE AND ROWNUM <= 1) AS STATUS_LINHA,");
			sql.append(
					" (CASE WHEN (SELECT FLAG FROM TBL_WHATSAPP T2 WHERE T2.TELEFONE = T.TELEFONE AND ROWNUM <= 1) = 1 THEN 1 ELSE 0 END) AS WHATSAPP, ");
			sql.append(
					" (SELECT IS_DATE_CONFIRME(DT_OBITO) FROM OBITO WHERE NU_CPF = CPFCGC AND ROWNUM<=1 ) AS OBITO,");
			sql.append(" (SELECT DT_ABERTURA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS DT_ABERTURA,");
			sql.append(" (SELECT SITUACAO FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS SITUACAO,");
			sql.append(" (SELECT FANTASIA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS FANTASIA,");
			sql.append(" (SELECT DESC_NATUREZA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS NATUREZA,");
			sql.append(
					" I.NOME,T.PROPRIETARIO AS PROPRIETARIO,T.ATUAL,T.TELEFONE,T.ENDERECO AS ENDERECO,T.NUMERO AS NUMERO,T.COMPLEMENTO,T.BAIRRO,T.CEP,T.CIDADE,T.UF,T.CPFCGC AS CPFCNPJ,");
			sql.append(
					" i.CPF_CONJUGE AS CPF_CONJUGE,PEGA_NOME_INFO_COMP(I.CPF_CONJUGE) AS NOME_CONJUGE,I.NOME_MAE AS MAE,TO_CHAR(I.DATA_NASC,'DD/MM/YYYY') AS NASC,' ' AS TITULO,T.OPERADORA,TO_CHAR(T.DT_INSTALACAO,'YYYY-MM-DD') AS DT_INSTALACAO, I.SIGNO AS SIGNO ");
			sql.append(" FROM TELEFONES T,INFO_COMPLEMENTARES I,FINAN.CRED_MEGA_CEP M ");
			sql.append(" WHERE T.CEP=M.CEP(+) AND T.CPFCGC=I.CPFCNPJ(+) ");
			sql.append(" AND T.CEP='" + this.getTelefone().get(indice).getCep() + "' AND T.NUMERO="
					+ this.getTelefone().get(indice).getNumero() + " AND T.CPFCGC<>'"
					+ this.infocomplementares.getCpfcnpj() + "' AND ROWNUM<=250");
			sql.append(" ) ORDER BY NUMERO, COMPLEMENTO, PROPRIETARIO ) PAGINA  WHERE ( ROWNUM <= "
					+ br.com.confirmeonline.util.SQLConstantes.QTD_MAX_PESQUISA + " ) ) WHERE ( PAGINA_RN >= '"
					+ paginaInicial + "' AND PAGINA_RN <= '" + paginaFinal + "' ) ");
			sql.append(" ORDER BY CPFCNPJ,NUMERO, COMPLEMENTO, PROPRIETARIO");

			/*
			 * Aciona o método processaConsulta que é responsável por ir ao banco de dados,
			 * ler as informações, e coloca-las dentro do managedBean By SMarcio em
			 * 30/09/2013
			 */

			ok = processaConsultaMoradores(new SqlToBind(), 1, mb, regra, this.getTelefone().get(indice), paginaInicial,
					paginaFinal);
			return ok;

		} catch (Exception e) {

			return false;

		}

	}

	public Boolean paginacao_sociedades(LoginMBean mb, Integer comando) {

		/*
		 * Método de paginacao Sociedades
		 *
		 *
		 * By SMarcio em 14/10/2013
		 */

		StringBuilder sql = new StringBuilder();
		StringBuilder sql2 = new StringBuilder();

		SqlToBind resultado = new SqlToBind();
		SqlToBind resultado2 = new SqlToBind();

		Boolean ok = false;
		Integer paginaFinal = 0;
		Integer paginaInicial = 0;
		Integer qtdpesq = 0;
		Integer pagina = 0;
		mb.setResposta_consulta(true);
		mb.setResposta_conArmazenada(false);
		mb.setResposta_endereco(false);
		mb.setResposta_nome(false);
		mb.setResposta_razao(false);
		mb.setResposta_cep(false);
		mb.setResposta_veiculo(false);
		mb.setResposta_historico_credito(false);
		try {

			qtdpesq = Integer.parseInt(mb.getQtdpesq());
			pagina = mb.getPaginaSociedades();

			if (qtdpesq < 1) {

				qtdpesq = 1;

			}
			if (qtdpesq > 100) {

				qtdpesq = 100;
			}
			/* Avança registro */

			/*
			 * Se o comando 0 for passado, sempre vai iniciar a pesquisa do inicio
			 */

			if (comando == 1) {

				pagina = pagina + 1;

			}

			/* Volta registro */
			if (comando == 2) {

				pagina = pagina - 1;
			}

			paginaFinal = qtdpesq * pagina;
			paginaInicial = (paginaFinal - qtdpesq) + 1;

			if (pagina == 1 || pagina < 1) {

				paginaFinal = qtdpesq;
				paginaInicial = 1;
				pagina = 1;
			}

			/* 250 e hardcode e o maximo de registros que pesquisamos */
			if (pagina > (250 / qtdpesq)) {

				pagina = 250 / qtdpesq;
				paginaFinal = 250;
				paginaInicial = (paginaFinal - qtdpesq) + 1;
			}

			if (comando == 0) {

				pagina = 1;
				paginaFinal = qtdpesq;
				paginaInicial = 1;

			}
			paginaFinal += 1;

			mb.setPaginaSociedades(pagina);
			sql.delete(0, sql.length());
			sql.append("SELECT * FROM ( ");
			sql.append(" SELECT PAGINA.*,ROWNUM PAGINA_RN FROM ( ");
			sql.append(" SELECT * FROM ( ");
			sql.append(
					" SELECT S.NOME,S.CPF,(SELECT STATUS FROM TELEFONES_STATUS WHERE TELEFONE = T.TELEFONE AND ROWNUM <= 1) AS STATUS_LINHA, ");
			sql.append(
					" (CASE WHEN (SELECT FLAG FROM TBL_WHATSAPP T2 WHERE T2.TELEFONE = T.TELEFONE AND ROWNUM <= 1) = 1 THEN 1 ELSE 0 END) AS WHATSAPP, ");
			sql.append(
					" T.OPERADORA,TO_CHAR(T.DT_INSTALACAO,'DD/MM/YYYY') AS DT_INSTALACAO,T.PROPRIETARIO,T.ENDERECO,T.NUMERO,T.COMPLEMENTO,T.BAIRRO,T.CIDADE,T.CEP,T.UF,T.ATUAL,T.TELEFONE,E.CNPJ,E.RAZAO_SOCIAL,E.FANTASIA,E.ENDERECO AS ENDERECO_EMPRESA");
			sql.append(
					" ,E.NUMERO NUMERO_EMPRESA,E.COMPLEMENTO AS COMPLEMENTO_EMPRESA,E.BAIRRO AS BAIRRO_EMPRESA,E.CIDADE AS CIDADE_EMPRESA ");
			sql.append(
					" ,E.UF AS UF_EMPRESA,TO_CHAR(TO_DATE(E.DT_ABERTURA,'DD/MM/YYYY'),'DD/MM/YYYY') AS DT_ABERTURA,E.RAMO_ATVI,E.DESC_RAMO,E.DESC_NATUREZA,E.SITUACAO,S.CARGO,S.PARTIC,TO_CHAR(S.ENTRADA,'DD/MM/YYYY') AS ENTRADA");
			sql.append(
					" FROM QSA_EMPRESAS E,QSA_SOCIOS S, TELEFONES T WHERE E.CNPJ = S.CNPJ AND CAST(S.CPF AS  VARCHAR2(14)) = T.CPFCGC(+) ");
			sql.append(" AND S.CPF = '" + mb.getPessoaSite().getCpfcnpj() + "' AND ROWNUM <= "
					+ br.com.confirmeonline.util.SQLConstantes.QTD_MAX_PESQUISA
					+ " ORDER BY CNPJ,TO_NUMBER(ATUAL) DESC ");
			sql.append(" ) ORDER BY CNPJ,TO_NUMBER(ATUAL) ) PAGINA  ) ");
			sql.append(" WHERE ( PAGINA_RN >= '" + paginaInicial + "' AND PAGINA_RN <= '" + paginaFinal
					+ "' )  ORDER BY CNPJ,TO_NUMBER(ATUAL) DESC ");

			sql2.append("SELECT * FROM ( ");
			sql2.append("SELECT PAGINA.*,ROWNUM PAGINA_RN FROM( ");
			sql2.append(
					"SELECT (SELECT STATUS FROM TELEFONES_STATUS WHERE TELEFONE = T.TELEFONE AND ROWNUM <= 1) AS STATUS_LINHA, (CASE WHEN (SELECT FLAG FROM TBL_WHATSAPP T2 WHERE T2.TELEFONE = T.TELEFONE AND ROWNUM <= 1) = 1 THEN 1 ELSE 0 END) AS WHATSAPP, (SELECT IS_DATE_CONFIRME(DT_OBITO) FROM OBITO WHERE NU_CPF = CPFCGC AND ROWNUM<=1 ) AS OBITO,(SELECT DT_ABERTURA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS DT_ABERTURA, ");
			sql2.append(
					"(SELECT SITUACAO FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS SITUACAO,(SELECT FANTASIA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS FANTASIA,(SELECT DESC_NATUREZA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS NATUREZA, ");
			sql2.append(
					"i.CPF_CONJUGE AS CPF_CONJUGE,PEGA_NOME_INFO_COMP(I.CPF_CONJUGE) AS NOME_CONJUGE,I.NOME,T.PROPRIETARIO AS PROPRIETARIO,T.ATUAL,T.TELEFONE,T.ENDERECO AS ENDERECO,T.NUMERO AS NUMERO,T.COMPLEMENTO,T.BAIRRO,T.CEP,T.CIDADE,T.UF,I.CPFCNPJ AS CPFCNPJ,I.NOME_MAE AS MAE,TO_CHAR(I.DATA_NASC,'DD/MM/YYYY') AS NASC,' ' AS TITULO,T.OPERADORA,TO_CHAR(T.DT_INSTALACAO,'YYYY-MM-DD') AS DT_INSTALACAO, ");
			sql2.append(
					" I.SIGNO AS SIGNO FROM TELEFONES T, INFO_COMPLEMENTARES I WHERE I.CPFCNPJ=T.CPFCGC(+) AND I.CPFCNPJ = '"
							+ mb.getPessoaSite().getCpfcnpj() + "'  ");
			sql2.append("ORDER BY TO_NUMBER(ATUAL) DESC) PAGINA ) ");
			sql2.append("WHERE  ( PAGINA_RN >= '" + paginaInicial + "' AND  PAGINA_RN <= '" + paginaFinal
					+ "' ) ORDER BY TO_NUMBER(ATUAL) DESC ");

			resultado.limpaLista();
			resultado.addString(mb.getPessoaSite().getCpfcnpj());
			resultado.addString(String.valueOf(br.com.confirmeonline.util.SQLConstantes.QTD_MAX_PESQUISA));
			resultado.addString(String.valueOf(paginaInicial));
			resultado.addString(String.valueOf(paginaFinal));

			resultado2.addString(mb.getPessoaSite().getCpfcnpj());
			resultado2.addString(String.valueOf(paginaInicial));
			resultado2.addString(String.valueOf(paginaFinal));

			resultado.setSql(sql.toString());
			resultado2.setSql(sql2.toString());

			/*
			 * Aciona o método processaConsulta Que é responsável por ir ao banco de dados,
			 * ler as informações, e coloca-las dentro do managedBean By SMarcio em
			 * 30/09/2013
			 */

			ok = processaConsultaSociedades(resultado, 1, mb);
			if (!ok) {
				ok = processaConsultaSociedades(resultado2, 1, mb);
			}

			return ok;

		} catch (Exception e) {
			logger.error("Erro no metodo paginacao_sociedades da classe resposta: " + e.getMessage());
			return false;

		}

	}

	public Boolean paginacao_referencia(LoginMBean mb, Integer comando) {

		/*
		 * Método de paginacao Sociedades
		 *
		 *
		 * By SMarcio em 14/10/2013
		 */

		String sql = null;
		Boolean ok = false;
		Integer paginaFinal = 0;
		Integer paginaInicial = 0;
		Integer qtdpesq = 0;
		Integer pagina = 0;
		mb.setResposta_consulta(true);
		mb.setResposta_conArmazenada(false);
		mb.setResposta_endereco(false);
		mb.setResposta_nome(false);
		mb.setResposta_razao(false);
		mb.setResposta_cep(false);
		mb.setResposta_veiculo(false);
		mb.setResposta_historico_credito(false);
		try {

			qtdpesq = Integer.parseInt(mb.getQtdpesq());
			pagina = mb.getPaginaSociedades();

			if (qtdpesq < 1) {

				qtdpesq = 1;

			}
			if (qtdpesq > 100) {

				qtdpesq = 100;
			}
			/* Avança registro */

			/*
			 * Se o comando 0 for passado, sempre vai iniciar a pesquisa do inicio
			 */

			if (comando == 1) {

				pagina = pagina + 1;

			}

			/* Volta registro */
			if (comando == 2) {

				pagina = pagina - 1;
			}

			paginaFinal = qtdpesq * pagina;
			paginaInicial = (paginaFinal - qtdpesq) + 1;

			if (pagina == 1 || pagina < 1) {

				paginaFinal = qtdpesq;
				paginaInicial = 1;
				pagina = 1;
			}

			/* 250 e hardcode e o maximo de registros que pesquisamos */
			if (pagina > (250 / qtdpesq)) {

				pagina = 250 / qtdpesq;
				paginaFinal = 250;
				paginaInicial = (paginaFinal - qtdpesq) + 1;
			}

			if (comando == 0) {

				pagina = 1;
				paginaFinal = qtdpesq;
				paginaInicial = 1;

			}
			paginaFinal += 1;

			mb.setPaginaTelRef(pagina);

			sql = "SELECT * FROM ( ";
			sql = sql + " SELECT PAGINA.*,ROWNUM PAGINA_RN FROM ( ";
			sql = sql
					+ " SELECT (SELECT STATUS FROM TELEFONES_STATUS WHERE TELEFONE = T.TELEFONE AND ROWNUM <= 1) AS STATUS_LINHA, ";
			sql = sql
					+ " (CASE WHEN (SELECT FLAG FROM TBL_WHATSAPP T2 WHERE T2.TELEFONE = T.TELEFONE AND ROWNUM <= 1) = 1 THEN 1 ELSE 0 END) AS WHATSAPP, ";
			sql = sql
					+ " (SELECT IS_DATE_CONFIRME(DT_OBITO) FROM OBITO WHERE NU_CPF = CPFCGC AND ROWNUM<=1 ) AS OBITO, ";
			sql = sql
					+ " (SELECT DT_ABERTURA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS DT_ABERTURA, ";
			sql = sql + " (SELECT SITUACAO FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS SITUACAO,";
			sql = sql + " (SELECT FANTASIA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS FANTASIA, ";
			sql = sql + " (SELECT DESC_NATUREZA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS NATUREZA,";
			sql = sql
					+ " I.NOME,T.PROPRIETARIO AS PROPRIETARIO,T.ATUAL,T.TELEFONE,T.ENDERECO AS ENDERECO,T.NUMERO AS NUMERO,T.COMPLEMENTO,T.BAIRRO,T.CEP,T.CIDADE,T.UF,T.CPFCGC AS CPFCNPJ,";
			sql = sql
					+ " i.CPF_CONJUGE AS CPF_CONJUGE,PEGA_NOME_INFO_COMP(I.CPF_CONJUGE) AS NOME_CONJUGE,I.NOME_MAE AS MAE,TO_CHAR(I.DATA_NASC,'DD/MM/YYYY') AS NASC,' ' AS TITULO,T.OPERADORA,TO_CHAR(T.DT_INSTALACAO,'YYYY-MM-DD') AS DT_INSTALACAO, I.SIGNO AS SIGNO ";
			sql = sql + " FROM DBCRED.TELEFONES T, DBCRED.INFO_COMPLEMENTARES I , DBCRED.TELEFONES_REFERENCIA R  ";
			sql = sql + " WHERE  ";
			sql = sql + " T.PROPRIETARIO IS NOT NULL      AND  ";
			sql = sql + " T.TELEFONE = R.TELEFONE         AND ";
			sql = sql + " T.CPFCGC = I.CPFCNPJ(+)         AND ";
			sql = sql + " T.CPFCGC <> '" + mb.getPessoaSite().getCpfcnpj() + "'       AND  ";
			sql = sql + " R.CPFCNPJ = '" + mb.getPessoaSite().getCpfcnpj()
					+ "' ORDER BY TO_NUMBER(ATUAL) DESC, WHATSAPP DESC ";
			sql = sql + " ) PAGINA WHERE ( ROWNUM <= " + br.com.confirmeonline.util.SQLConstantes.QTD_MAX_PESQUISA
					+ " ) ";
			sql = sql + " ) WHERE  ( PAGINA_RN >=  " + paginaInicial + "  AND  PAGINA_RN <= " + paginaFinal
					+ " ) ORDER BY TO_NUMBER(ATUAL) DESC ";

			/*
			 * Aciona o método processaConsulta Que é responsável por ir ao banco de dados,
			 * ler as informações, e coloca-las dentro do managedBean By SMarcio em
			 * 30/09/2013
			 */

			ok = processaConsultaTelRefAux(sql, 1, mb);
			return ok;

		} catch (Exception e) {

			return false;

		}

	}

	public Boolean paginacao_comercial(LoginMBean mb, Integer comando) {

		/*
		 * Método de paginacao Telefones Comerciais
		 *
		 *
		 * By SMarcio em 14/10/2013
		 */

		StringBuilder sql = new StringBuilder();
		SqlToBind resultado = new SqlToBind();
		Boolean ok = false;
		Integer paginaFinal = 0;
		Integer paginaInicial = 0;
		Integer qtdpesq = 0;
		Integer pagina = 0;
		mb.setResposta_consulta(true);
		mb.setResposta_conArmazenada(false);
		mb.setResposta_endereco(false);
		mb.setResposta_nome(false);
		mb.setResposta_razao(false);
		mb.setResposta_cep(false);
		mb.setResposta_veiculo(false);
		mb.setResposta_historico_credito(false);
		try {

			qtdpesq = Integer.parseInt(mb.getQtdpesq());
			pagina = mb.getPaginaTelCom();

			if (qtdpesq < 1) {

				qtdpesq = 1;

			}
			if (qtdpesq > 100) {

				qtdpesq = 100;
			}
			/* Avança registro */

			/*
			 * Se o comando 0 for passado, sempre vai iniciar a pesquisa do inicio
			 */

			if (comando == 1) {

				pagina = pagina + 1;

			}

			/* Volta registro */
			if (comando == 2) {

				pagina = pagina - 1;
			}

			paginaFinal = qtdpesq * pagina;
			paginaInicial = (paginaFinal - qtdpesq) + 1;

			if (pagina == 1 || pagina < 1) {

				paginaFinal = qtdpesq;
				paginaInicial = 1;
				pagina = 1;
			}

			/* 250 e hardcode e o maximo de registros que pesquisamos */
			if (pagina > (250 / qtdpesq)) {

				pagina = 250 / qtdpesq;
				paginaFinal = 250;
				paginaInicial = (paginaFinal - qtdpesq) + 1;
			}

			if (comando == 0) {

				pagina = 1;
				paginaFinal = qtdpesq;
				paginaInicial = 1;

			}
			paginaFinal += 1;

			mb.setPaginaTelCom(pagina);

			sql.append(" SELECT * FROM ( ");
			sql.append(" SELECT PAGINA.*,ROWNUM PAGINA_RN FROM ( ");
			sql.append(
					" SELECT (SELECT STATUS FROM TELEFONES_STATUS WHERE TELEFONE = T.TELEFONE AND ROWNUM <= 1) AS STATUS_LINHA, ");
			sql.append(
					" (CASE WHEN (SELECT FLAG FROM TBL_WHATSAPP T2 WHERE T2.TELEFONE = T.TELEFONE AND ROWNUM <= 1) = 1 THEN 1 ELSE 0 END) AS WHATSAPP, ");
			sql.append(
					" (SELECT DT_ABERTURA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS DT_ABERTURA, ");
			sql.append(" (SELECT SITUACAO FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS SITUACAO, ");
			sql.append(" (SELECT FANTASIA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS FANTASIA, ");
			sql.append(" (SELECT DESC_NATUREZA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS NATUREZA, ");
			sql.append(
					" T.PROPRIETARIO AS PROPRIETARIO,T.ATUAL,T.TELEFONE,T.ENDERECO AS ENDERECO,T.NUMERO AS NUMERO,T.COMPLEMENTO,T.BAIRRO,T.CEP,T.CIDADE,T.UF,T.CPFCGC AS CPFCNPJ, ");
			sql.append(" ' ' AS TITULO,T.OPERADORA,TO_CHAR(T.DT_INSTALACAO,'YYYY-MM-DD') AS DT_INSTALACAO  ");

			sql.append(
					" FROM TELEFONES T,TELEFONES_COMERCIAIS C WHERE T.CPFCGC = C.CNPJ AND C.CPF = ? ORDER BY TO_NUMBER(ATUAL) DESC ");
			sql.append(" ) PAGINA )");
			sql.append(
					" ) WHERE  ( PAGINA_RN >= ? AND  PAGINA_RN <= ? ) ORDER BY TO_NUMBER(ATUAL) DESC, WHATSAPP DESC ");

			resultado.limpaLista();
			resultado.addString(mb.getPessoaSite().getCpfcnpj());
			resultado.addString(String.valueOf(paginaInicial));
			resultado.addString(String.valueOf(paginaFinal));

			resultado.setSql(sql.toString());

			ok = processaConsultaTelefonesComerciais(resultado, 1, mb);

			return ok;

		} catch (Exception e) {

			return false;

		}

	}

	public Boolean paginacao_imoveis(LoginMBean mb, Integer comando) {

		/*
		 * Método de paginacao Sociedades
		 *
		 *
		 * By SMarcio em 06/11/2013
		 */

		StringBuilder sql = new StringBuilder();
		Boolean ok = false;
		Integer paginaFinal = 0;
		Integer paginaInicial = 0;
		Integer qtdpesq = 0;
		Integer pagina = 0;
		mb.setResposta_consulta(true);
		mb.setResposta_conArmazenada(false);
		mb.setResposta_endereco(false);
		mb.setResposta_nome(false);
		mb.setResposta_razao(false);
		mb.setResposta_cep(false);
		mb.setResposta_veiculo(false);
		mb.setResposta_historico_credito(false);
		try {

			qtdpesq = Integer.parseInt(mb.getQtdpesq());
			pagina = mb.getPaginaImoveis();

			if (qtdpesq < 1) {

				qtdpesq = 1;

			}
			if (qtdpesq > 100) {

				qtdpesq = 100;
			}
			/* Avança registro */

			/*
			 * Se o comando 0 for passado, sempre vai iniciar a pesquisa do inicio
			 */

			if (comando == 1) {

				pagina = pagina + 1;

			}

			/* Volta registro */
			if (comando == 2) {

				pagina = pagina - 1;
			}

			paginaFinal = qtdpesq * pagina;
			paginaInicial = (paginaFinal - qtdpesq) + 1;

			if (pagina == 1 || pagina < 1) {

				paginaFinal = qtdpesq;
				paginaInicial = 1;
				pagina = 1;
			}

			/* 250 e hardcode e o maximo de registros que pesquisamos */
			if (pagina > (250 / qtdpesq)) {

				pagina = 250 / qtdpesq;
				paginaFinal = 250;
				paginaInicial = (paginaFinal - qtdpesq) + 1;
			}

			if (comando == 0) {

				pagina = 1;
				paginaFinal = qtdpesq;
				paginaInicial = 1;

			}
			paginaFinal += 1;

			mb.setPaginaImoveis(pagina);
			sql.append(" SELECT * FROM ( ");
			sql.append(" SELECT PAGINA.*,ROWNUM PAGINA_RN FROM ( ");
			sql.append(
					" select i.CPF, i.NOME, i.ENDERECO, i.CEP, i.SETOR, i.QUADRA, i.LOTE, i.CODIGO_LOG, i.AREA_TERRENO, i.AREA_CONSTRUIDA, i.ANO_CONSTRUCAO,i.BASE_CALCULO, TO_CHAR(TO_DATE(i.DIA_VENCIMENTO,'DD/MM/YYYY'), 'DD/MM/YYYY') AS DIA_VENCIMENTO, i.TESTADA, i.FRACAO_IDEAL, i.INSCRICAO ");
			sql.append(" from iptu i where cpf ='" + mb.getPessoaSite().getCpfcnpj() + "' AND ( ROWNUM <= "
					+ br.com.confirmeonline.util.SQLConstantes.QTD_MAX_PESQUISA + " )");
			sql.append(" ) PAGINA  ");
			sql.append(" ) WHERE  ( PAGINA_RN >= " + paginaInicial + " AND  PAGINA_RN <= " + paginaFinal + " ) ");
			ok = processaConsultaImovel(sql.toString(), 1, mb);
			return ok;

		} catch (Exception e) {

			return false;

		}

	}

	public Boolean paginacao_veiculos(LoginMBean mb, Integer comando) {

		/*
		 * Método de paginacao Veiculos
		 *
		 * By SMarcio em 30/10/2013
		 */

		StringBuilder sql = new StringBuilder();
		Boolean ok = false;
		Integer paginaFinal = 0;
		Integer paginaInicial = 0;
		Integer qtdpesq = 0;
		Integer pagina = 0;
		mb.setResposta_consulta(true);
		mb.setResposta_conArmazenada(false);
		mb.setResposta_endereco(false);
		mb.setResposta_nome(false);
		mb.setResposta_razao(false);
		mb.setResposta_cep(false);
		mb.setResposta_veiculo(false);
		mb.setResposta_historico_credito(false);
		try {

			qtdpesq = Integer.parseInt(mb.getQtdpesq());
			pagina = mb.getPaginaVeiculos();

			if (qtdpesq < 1) {

				qtdpesq = 1;

			}
			if (qtdpesq > 100) {

				qtdpesq = 100;
			}
			/* Avança registro */

			/*
			 * Se o comando 0 for passado, sempre vai iniciar a pesquisa do inicio
			 */

			if (comando == 1) {

				pagina = pagina + 1;

			}

			/* Volta registro */
			if (comando == 2) {

				pagina = pagina - 1;
			}

			paginaFinal = qtdpesq * pagina;
			paginaInicial = (paginaFinal - qtdpesq) + 1;

			if (pagina == 1 || pagina < 1) {

				paginaFinal = qtdpesq;
				paginaInicial = 1;
				pagina = 1;
			}

			/* 250 e hardcode e o maximo de registros que pesquisamos */
			if (pagina > (250 / qtdpesq)) {

				pagina = 250 / qtdpesq;
				paginaFinal = 250;
				paginaInicial = (paginaFinal - qtdpesq) + 1;
			}

			if (comando == 0) {

				pagina = 1;
				paginaFinal = qtdpesq;
				paginaInicial = 1;

			}
			paginaFinal += 1;

			mb.setPaginaVeiculos(pagina);

			sql.append("SELECT * FROM ( ");
			sql.append(" SELECT PAGINA.*,ROWNUM PAGINA_RN FROM (  ");
			sql.append(" SELECT * FROM ( ");
			sql.append(" SELECT V.PLACA,V.MARCA,V.RENAVAN,V.ANOFAB,V.CHASSI,V.COMBU,V.ANOMODE,V.PROPRI, ");
			sql.append(" V.END,V.NUM,V.COMPL,V.BAIRRO,V.CEP,V.CPF AS CPFCNPJ,V.CIDADE,V.ESTADO,V.DAINCL,V.DALICE ");
			sql.append(" FROM VEICULOS V  ");
			sql.append(" WHERE V.CPF = '" + mb.getPessoaSite().getCpfcnpj() + "' AND ( ROWNUM <= "
					+ br.com.confirmeonline.util.SQLConstantes.QTD_MAX_PESQUISA + " )");
			sql.append(" )  ) PAGINA   ) WHERE ( PAGINA_RN >= '" + paginaInicial + "' AND PAGINA_RN <= '" + paginaFinal
					+ "' ) ");
			ok = processaConsultaVeiculos(sql.toString(), 1, mb);

			return ok;

		} catch (Exception e) {

			return false;
		}

	}

	public Boolean paginacao_veiculos_completo(LoginMBean mb, Integer comando) {

		/*
		 * Método de paginacao Veiculos
		 *
		 * By SMarcio em 13/11/2013
		 */

		StringBuilder sql = new StringBuilder();
		Boolean ok = false;
		Integer paginaFinal = 0;
		Integer paginaInicial = 0;
		Integer qtdpesq = 0;
		Integer pagina = 0;
		mb.setResposta_consulta(true);
		mb.setResposta_conArmazenada(false);
		mb.setResposta_endereco(false);
		mb.setResposta_nome(false);
		mb.setResposta_razao(false);
		mb.setResposta_cep(false);
		mb.setResposta_veiculo(false);
		mb.setResposta_historico_credito(false);
		boolean ANDFLG = false;
		String[] armazenados = new String[5];
		Integer posix = 0;

		this.setTabConArmazenada(this.getTabConArmazenada() + 1);
		if (this.getTabConArmazenada() > 5) {

			this.setTabConArmazenada(1);

		}
		armazenados = this.getCpfcnpjArmazenado();
		try {

			posix = this.getTabConArmazenada() - 1;
			if (posix < 0) {

				posix = 0;

			}

		} catch (Exception e) {

			posix = 0;

		}

		if (comando == 0) {

			armazenados[posix] = "Veiculos";

		}

		try {

			qtdpesq = Integer.parseInt(mb.getQtdpesq());
			pagina = mb.getPaginaVeiculosCompleto();

			if (qtdpesq < 1) {

				qtdpesq = 1;

			}
			if (qtdpesq > 100) {

				qtdpesq = 100;
			}
			/* Avança registro */

			/*
			 * Se o comando 0 for passado, sempre vai iniciar a pesquisa do inicio
			 */

			if (comando == 1) {

				pagina = pagina + 1;

			}

			/* Volta registro */
			if (comando == 2) {

				pagina = pagina - 1;
			}

			paginaFinal = qtdpesq * pagina;
			paginaInicial = (paginaFinal - qtdpesq) + 1;

			if (pagina == 1 || pagina < 1) {

				paginaFinal = qtdpesq;
				paginaInicial = 1;
				pagina = 1;
			}

			/* 250 e hardcode e o maximo de registros que pesquisamos */
			if (pagina > (250 / qtdpesq)) {

				pagina = 250 / qtdpesq;
				paginaFinal = 250;
				paginaInicial = (paginaFinal - qtdpesq) + 1;
			}

			if (comando == 0) {

				pagina = 1;
				paginaFinal = qtdpesq;
				paginaInicial = 1;

			}

			mb.setPaginaVeiculosCompleto(pagina);

			sql.append("SELECT * FROM ( ");
			sql.append(" SELECT PAGINA.*,ROWNUM PAGINA_RN FROM (  ");
			sql.append(" SELECT * FROM ( ");
			sql.append(" SELECT V.PLACA,V.MARCA,V.RENAVAN,V.ANOFAB,V.CHASSI,V.COMBU,V.ANOMODE,V.PROPRI, ");
			sql.append(" V.END,V.NUM,V.COMPL,V.BAIRRO,V.CEP,V.CPF AS CPFCNPJ,V.CIDADE,V.ESTADO,V.DAINCL,V.DALICE ");
			sql.append(" FROM VEICULOS V WHERE ");

			if (mb.getPessoaSite().getVeiculo().getCpfcnpj().isEmpty() == false) {
				String upperCaseCpfCnpj = mb.getPessoaSite().getVeiculo().getCpfcnpj().toUpperCase();
				mb.getPessoaSite().getVeiculo().setCpfcnpj(upperCaseCpfCnpj);

				sql.append(" V.CPF = '" + mb.getPessoaSite().getVeiculo().getCpfcnpj() + "'");
				ANDFLG = true;
			}

			if (mb.getPessoaSite().getVeiculo().getRenavam().isEmpty() == false) {

				if (ANDFLG == true) {
					sql.append(" AND ");
				}
				sql.append(" V.RENAVAN = '" + mb.getPessoaSite().getVeiculo().getRenavam() + "'");
				ANDFLG = true;
			}

			if (mb.getPessoaSite().getVeiculo().getPlaca().isEmpty() == false) {
				String upperCasePlaca = mb.getPessoaSite().getVeiculo().getPlaca().toUpperCase();
				mb.getPessoaSite().getVeiculo().setPlaca(upperCasePlaca.replace(" ", ""));

				if (ANDFLG == true) {
					sql.append(" AND ");
				}
				sql.append(" V.PLACA = '" + mb.getPessoaSite().getVeiculo().getPlaca() + "'");
				ANDFLG = true;
			}

			if (mb.getPessoaSite().getVeiculo().getChassi().isEmpty() == false) {
				String upperCaseChassi = mb.getPessoaSite().getVeiculo().getChassi().toUpperCase();
				mb.getPessoaSite().getVeiculo().setChassi(upperCaseChassi);

				if (ANDFLG == true) {
					sql.append(" AND ");
				}
				sql.append(" V.CHASSI = '" + mb.getPessoaSite().getVeiculo().getChassi() + "'");
				ANDFLG = true;
			}

			if (mb.getPessoaSite().getVeiculo().getNome().isEmpty() == false) {
				String upperCaseNome = mb.getPessoaSite().getVeiculo().getNome().toUpperCase();
				mb.getPessoaSite().getVeiculo().setNome(upperCaseNome);

				if (ANDFLG == true) {
					sql.append(" AND ");
				}
				sql.append(" V.PROPRI LIKE '" + mb.getPessoaSite().getVeiculo().getNome() + "'");
				ANDFLG = true;
			}

			// if ( ANDFLG == true ){ sql.append(" AND "); }

			sql.append(" AND ROWNUM <= " + br.com.confirmeonline.util.SQLConstantes.QTD_MAX_PESQUISA + " ");
			sql.append(" )  ) PAGINA ) WHERE ( PAGINA_RN >= '" + paginaInicial + "' AND PAGINA_RN <= '" + paginaFinal
					+ "' ) ");

			ok = processaConsultaVeiculos(sql.toString(), 1, mb);

			/* Registra a consulta */
			String[] nomeservidor = mb.getServidor();
			Conexao.registraConsulta(this.getConnection(), "VEICULOS", mb.getPessoaSite().getCpfcnpj(),
					mb.getUsuario().getLogin(), mb.getUsuario().getSenha(), "CONFI", mb.getUsuario().getIP(),
					mb.getCanonicalName());

			mb.setResposta_consulta(false);
			mb.setResposta_conArmazenada(false);
			mb.setResposta_endereco(false);
			mb.setResposta_nome(false);
			mb.setResposta_razao(false);
			mb.setResposta_cep(false);
			mb.setResposta_veiculo(ok);
			mb.setResposta_historico_credito(false);
			this.setForm_active_cpfcnpj("form");
			this.setForm_active_telefone("form");
			this.setForm_active_operadora("form");
			this.setForm_active_cep("form");
			this.setForm_active_endereco("form");
			this.setForm_active_historico_credito("form");
			this.setForm_active_nome("form");
			this.setForm_active_razao_social("form");
			this.setForm_active_veiculos("form active");

		} catch (Exception e) {
			ok = false;

		} finally {
			releaseConnection();
		}
		return ok;

	}

	public Boolean processaConsultaSociedades(SqlToBind consulta, int tipopesquisa, LoginMBean mx) throws SQLException {

		/*
		 * Método de pesquisa Sociedades
		 *
		 * By SMarcio em 05/11/2013
		 */

		java.sql.PreparedStatement stmtN = null;
		ResultSet rs = null;
		int ind = 0;
		String aux = "";
		String anobase = "";
		int atualizacaoTel = 0;
		int atualizacaoBase = 0;
		String razaosocial = "";
		String celular = "";
		String dtn = "";
		String dts = "";
		String ets = "";
		String areaterreno = "";
		String basecalc = "";
		String diavencimento = "";
		String fracaoideal = "";
		String daincl = "";
		String dalice = "";
		String sql = "";
		String sexo = "";
		String dtabertura = "";
		String cpfcnpj = "";
		String cpfcnpjant = "";
		String ramoatividade = "";
		Boolean achou = false;
		int id = 0;
		Connection conn = this.getConnection();
		Boolean requestFromViewSingleButton = false;
		if (!possuiConexao) {
			setPossuiConexao(true);
			requestFromViewSingleButton = true;
		}
		try {

			stmtN = conn.prepareStatement(consulta.getSql());

			for (int i = 0; i <= consulta.getBinds().size() - 1; i++)
				stmtN.setString(i + 1, consulta.getBinds().get(i));

			rs = stmtN.executeQuery();

			ind = 0;
			Telefone t;
			Sociedades s;

			if (tipopesquisa == 1) {

				this.sociedades = new ArrayList<Sociedades>();
			}

			while (rs != null && rs.next()) {
				id++;
				t = new Telefone();
				s = new Sociedades();

				/*
				 * tipos de pesquisa 1 - pesquisa por telefone 2 - parentes
				 *
				 */

				/* Pesquisa Sociedades ConfirmeOnLine New */

				cpfcnpj = this.Filtra(rs.getString("CNPJ") == null ? "" : rs.getString("CNPJ"));

				if (!cpfcnpj.equals(cpfcnpjant)) {

					s.setEoprimeiro(true);
					t.setEoprimeiro(true);
					cpfcnpjant = cpfcnpj;
					id = 1;

				}
				razaosocial = this.Filtra(rs.getString("RAZAO_SOCIAL") == null ? "" : rs.getString("RAZAO_SOCIAL"));
				s.setRazao_social(razaosocial);
				s.setCnpj(rs.getString("CNPJ") == null ? "" : rs.getString("CNPJ"));

				if (this.infocomplementares.getNome() == null || this.infocomplementares.getNome().equals("")) {
					this.infocomplementares.setNome(razaosocial);
					this.infocomplementares.setCpfcnpj(s.getCnpj());
				}

				s.setDtabertura(rs.getString("DT_ABERTURA") == null ? "" : rs.getString("DT_ABERTURA"));
				s.setNome_fantasia(this.Filtra(rs.getString("FANTASIA") == null ? "" : rs.getString("FANTASIA")));
				s.setDesc_natureza(
						this.Filtra(rs.getString("DESC_NATUREZA") == null ? "" : rs.getString("DESC_NATUREZA")));
				s.setSituacao(this.Filtra(rs.getString("SITUACAO") == null ? "" : rs.getString("SITUACAO")));
				s.setCnpj(this.Filtra(rs.getString("CNPJ") == null ? "" : rs.getString("CNPJ")));
				s.setEndereco_empresa(
						this.Filtra(rs.getString("ENDERECO_EMPRESA") == null ? "" : rs.getString("ENDERECO_EMPRESA")));
				s.setNumero_empresa(
						this.Filtra(rs.getString("NUMERO_EMPRESA") == null ? "" : rs.getString("NUMERO_EMPRESA")));
				s.setComplemento_empresa(this.Filtra(
						rs.getString("COMPLEMENTO_EMPRESA") == null ? "" : rs.getString("COMPLEMENTO_EMPRESA")));
				s.setBairro_empresa(
						this.Filtra(rs.getString("BAIRRO_EMPRESA") == null ? "" : rs.getString("BAIRRO_EMPRESA")));
				s.setCidade_empresa(
						this.Filtra(rs.getString("CIDADE_EMPRESA") == null ? "" : rs.getString("CIDADE_EMPRESA")));
				// s.setCep_empresa(cpfcnpjant); ainda nao temos cep nesta
				// tabela mas ja fica pronto
				s.setUf_empresa(this.Filtra(rs.getString("UF_EMPRESA") == null ? "" : rs.getString("UF_EMPRESA")));

				ramoatividade = this.Filtra(rs.getString("RAMO_ATVI") == null ? "" : rs.getString("RAMO_ATVI"));
				ramoatividade = this.GET_RAMO_MD_NOVO(ramoatividade, mx);
				s.setRamo_atividade(ramoatividade);

				s.setCargo_socio(this.Filtra(rs.getString("CARGO") == null ? "" : rs.getString("CARGO")));
				s.setParticipacao(this.Filtra(rs.getString("PARTIC") == null ? "" : rs.getString("PARTIC")));
				s.setEntrada_sociedade(this.Filtra(rs.getString("ENTRADA") == null ? "" : rs.getString("ENTRADA")));
				t.setId(id);
				t.setProprietario(
						this.Filtra(rs.getString("PROPRIETARIO") == null ? "" : rs.getString("PROPRIETARIO")));
				t.setEndereco(this.Filtra(rs.getString("ENDERECO") == null ? "" : rs.getString("ENDERECO")));
				t.setNumero(this.Filtra(rs.getString("NUMERO") == null ? "" : rs.getString("NUMERO")));
				t.setComplemento(this.Filtra(rs.getString("COMPLEMENTO") == null ? "" : rs.getString("COMPLEMENTO")));
				t.setBairro(this.Filtra(rs.getString("BAIRRO") == null ? "" : rs.getString("BAIRRO")));
				t.setCidade(this.Filtra(rs.getString("CIDADE") == null ? "" : rs.getString("CIDADE")));
				t.setCep(this.Filtra(rs.getString("CEP") == null ? "" : rs.getString("CEP")));
				t.setUf(this.Filtra(rs.getString("UF") == null ? "" : rs.getString("UF")));
				t.setCpfcnpj(cpfcnpj);
				t.setNumeroTelefone(
						this.Filtra(rs.getString("TELEFONE") == null ? "-----------" : rs.getString("TELEFONE")));

				t.setStatusLinha(this.Filtra(rs.getString("STATUS_LINHA") == null ? "" : rs.getString("STATUS_LINHA")));
				t.setAtual(this.Filtra(rs.getString("ATUAL") == null ? "" : rs.getString("ATUAL")));

				/*
				 * Verifica se o cliente possui direito a base que ele esta consultando, se não
				 * mostra alguns dados mas os endereços e telefones não mostra
				 */

				if (Conexao.GetSerial(conn, mx.getUsuario().getLogin()).indexOf(t.getUf()) != -1) {

					t.setNumeroTelefone(
							this.Filtra(rs.getString("TELEFONE") == null ? "-----------" : rs.getString("TELEFONE")));
					t.setEndereco(this.Filtra(rs.getString("ENDERECO") == null ? "" : rs.getString("ENDERECO")));
					t.setNumero(this.Filtra(rs.getString("NUMERO") == null ? "" : rs.getString("NUMERO")));
					t.setComplemento(
							this.Filtra(rs.getString("COMPLEMENTO") == null ? "" : rs.getString("COMPLEMENTO")));
					t.setBairro(this.Filtra(rs.getString("BAIRRO") == null ? "" : rs.getString("BAIRRO")));
					t.setCidade(this.Filtra(rs.getString("CIDADE") == null ? "" : rs.getString("CIDADE")));
					t.setCep(this.Filtra(rs.getString("CEP") == null ? "" : rs.getString("CEP")));
					t.setProcon(this.proconSP(t.getNumeroTelefone(), conn));
					if (t.getNumeroTelefone().length() < 9) {

						t.setNumeroTelefone("-----------");

					}

				} else {

					t.setNumeroTelefone("--Estado não contratado.");
					t.setEndereco("Estado não contratado ");
					t.setNumero("-");
					t.setComplemento("-");
					t.setBairro("-");
					t.setCidade("-");
					t.setCep("-");
					t.setProcon("-");

				}

				t.setRatingTelefone(t.getAtual(), getUseStatus());
				t.setWhatsApp(rs.getString("WHATSAPP"));
				s.setTelefonesociedades(t);
				this.sociedades.add(s);

				ind++;

			}

			this.setPaginasociedadesAnt(false);
			this.setPaginasociedadesProx(false);
			if (!(mx.getPaginaSociedades().equals(1)) && this.sociedades.size() > 0) {
				this.setPaginasociedadesAnt(true);

			}
			if (this.sociedades.size() > Integer.valueOf(mx.getQtdpesq())) {
				this.setPaginasociedadesProx(true);
				this.sociedades.remove(this.sociedades.size() - 1);
				paginasociedadesProx = true;
			}
		} catch (SQLException e) {
			logger.error("Erro no metodo processaConsultaSociedades da classe Resposta: " + e.getMessage());
			achou = false;
		} finally {
			if (requestFromViewSingleButton) {
				setPossuiConexao(false);
			}

			if (rs != null && !rs.isClosed())
				rs.close();
			if (stmtN != null && !stmtN.isClosed())
				stmtN.close();

			releaseConnection();
		}
		if (id > 0) {
			achou = true;
		} else {
			achou = false;
		}
		return achou;
	}

	@Deprecated
	public Boolean processaConsultaSocios(SqlToBind consulta, int tipopesquisa, LoginMBean mx) throws SQLException {

		/*
		 * Método de pesquisa Sociedades
		 *
		 * By SMarcio em 05/11/2013
		 */

		java.sql.PreparedStatement stmtN = null;
		ResultSet rs = null;
		int ind = 0;
		String aux = "";
		String anobase = "";
		int atualizacaoTel = 0;
		int atualizacaoBase = 0;
		String razaosocial = "";
		String celular = "";
		String dtn = "";
		String dts = "";
		String ets = "";
		String areaterreno = "";
		String basecalc = "";
		String diavencimento = "";
		String fracaoideal = "";
		String daincl = "";
		String dalice = "";
		String sql = "";
		String sexo = "";
		String dtabertura = "";
		String cpfcnpj = "";
		String cpfcnpjant = "";
		String ramoatividade = "";
		int id = 0;
		Boolean achou = false;
		Boolean requestFromViewSingleButton = false;
		try {

			stmtN = this.getConnection().prepareStatement(consulta.getSql());
			if (!possuiConexao) {
				setPossuiConexao(true);
				requestFromViewSingleButton = true;
			}

			for (int i = 0; i <= consulta.getBinds().size() - 1; i++)
				stmtN.setString(i + 1, consulta.getBinds().get(i));

			rs = stmtN.executeQuery();
			ind = 0;
			Socios s;

			if (tipopesquisa == 1) {

				this.socios = new ArrayList<Socios>();
			}

			while (rs != null && rs.next()) {
				id++;
				s = new Socios();

				/* Pesquisa Socios ConfirmeOnLine New */

				s.setCnpj(this.Filtra(rs.getString("CNPJ") == null ? "" : rs.getString("CNPJ")));
				s.setCpf(this.Filtra(rs.getString("CPF") == null ? "" : rs.getString("CPF")));
				s.setNome(this.Filtra(rs.getString("NOME") == null ? "" : rs.getString("NOME")));
				s.setCargo(this.Filtra(rs.getString("CARGO") == null ? "" : rs.getString("CARGO")));
				s.setParticipacao(this.Filtra(rs.getString("PARTIC") == null ? "" : rs.getString("PARTIC")));
				s.setDt_entrada(rs.getString("ENTRADA") == null ? "" : rs.getString("ENTRADA"));
				this.socios.add(s);
				if (this.infocomplementares.getNome() == null || this.infocomplementares.getNome().equals("")) {
					this.infocomplementares
							.setNome(rs.getString("RAZAO_SOCIAL") == null ? "" : rs.getString("RAZAO_SOCIAL"));
					this.infocomplementares.setCpfcnpj(s.getCnpj());
				}

				ind++;

			}

			/* verifica se vai ser necessário colocar paginacao na tela */

			// if ( ind <Integer.parseInt(mx.getQtdpesq()) ){
			//
			// this.setPaginasocios(false);
			//
			//
			// }else{
			//
			// this.setPaginasocios(true);
			//
			// }

			this.setPaginasociosAnt(false);
			this.setPaginasociosProx(false);
			if (!(mx.getPaginaSocios().equals(1)) && this.sociedades.size() > 0) {
				this.setPaginasociosAnt(true);
			}
			if (this.sociedades.size() > Integer.valueOf(mx.getQtdpesq())) {
				this.setPaginasociedadesProx(true);
				this.sociedades.remove(this.sociedades.size() - 1);
			}

		} catch (SQLException e) {

			achou = false;
		} finally {
			if (requestFromViewSingleButton) {
				setPossuiConexao(false);
			}

			if (rs != null)
				rs.close();
			if (stmtN != null)
				stmtN.close();
			releaseConnection();
		}
		if (id > 0) {

			achou = true;

		} else {

			achou = false;

		}
		return achou;

	}

	public Boolean processaConsultaSocios2(SqlToBind consulta, int tipopesquisa, LoginMBean mx)
			throws ParseException, SQLException {

		/*
		 * Método de pesquisa Sociedades
		 *
		 * By SMarcio em 05/11/2013
		 */

		java.sql.PreparedStatement stmtN = null;
		ResultSet rs = null;
		int ind = 0;
		String aux = "";
		String anobase = "";
		int atualizacaoTel = 0;
		int atualizacaoBase = 0;
		String razaosocial = "";
		String celular = "";
		String dtn = "";
		String dts = "";
		String ets = "";
		String areaterreno = "";
		String basecalc = "";
		String diavencimento = "";
		String fracaoideal = "";
		String daincl = "";
		String dalice = "";
		String sql = "";
		String sexo = "";
		String dtabertura = "";
		String cpfcnpj = "";
		String cpfcnpjant = "";
		String ramoatividade = "";
		int id = 0;
		Boolean achou = false;
		Boolean requestFromViewSingleButton = false;
		Double valor = 0.0;

		String sCpf = "";
		String sCNPJ = "";
		String sRazaoSocial = "";

		try {

			stmtN = this.getConnection().prepareStatement(consulta.getSql());
			if (!possuiConexao) {
				setPossuiConexao(true);
				requestFromViewSingleButton = true;
			}

			for (int i = 0; i <= consulta.getBinds().size() - 1; i++)
				stmtN.setString(i + 1, consulta.getBinds().get(i));

			rs = stmtN.executeQuery();
			ind = 0;
			Socios s;

			if (tipopesquisa == 1) {

				this.socios = new ArrayList<Socios>();
			}

			while (rs != null && rs.next()) {
				id++;
				s = new Socios();

				if (!Strings.isNullOrEmpty(rs.getString("PARTIC"))) {
					try {

						try {
							valor = Double.parseDouble(rs.getString("PARTIC"));
						} catch (Exception e) {
							System.out.println(
									"Erro no metodo processaConsultaSocio2 da classe Resposta: " + e.getMessage());
						}

					} catch (Exception e) {
						// TODO Auto-generated catch block

						System.out
								.println("Erro no metodo processaConsultaSocio2 da classe Resposta: " + e.getMessage());
					}
				}

				/* Pesquisa Socios ConfirmeOnLine New */

				sCpf = rs.getString("CPF");
				sCNPJ = rs.getString("CNPJ");
				sRazaoSocial = rs.getString("RAZAO_SOCIAL");

				s.setCnpj(this.Filtra(rs.getString("CNPJ") == null ? "" : rs.getString("CNPJ")));

				s.setCpf(this.Filtra(rs.getString("CPF") == null ? "" : rs.getString("CPF")));

				if (mx.getPessoaSite().getCpfcnpj().length() == 11) {
					s.setNome(this.Filtra(rs.getString("RAZAO_SOCIAL") == null ? "" : rs.getString("RAZAO_SOCIAL")));
					s.setCpf(this.Filtra(rs.getString("CNPJ") == null ? "" : rs.getString("CNPJ")));
				} else if (mx.getPessoaSite().getCpfcnpj().length() == 14) {
					s.setCpf(this.Filtra(rs.getString("CPF") == null ? "" : rs.getString("CPF")));
					s.setNome(this.Filtra(rs.getString("NOME") == null ? "" : rs.getString("NOME")));
				}

				s.setCargo(this.Filtra(rs.getString("CARG_DS_CARGO") == null ? "" : rs.getString("CARG_DS_CARGO")));
				s.setParticipacao(this.Filtra(rs.getString("PARTIC") == null ? "" : valor.toString()));
				s.setDt_entrada(rs.getString("ENTRADA") == null ? ""
						: Utils.formatDateString(Constantes.yyyyMMdd, Constantes.ddMMyyyy, rs.getString("ENTRADA")));

				this.socios.add(s);
				if (this.infocomplementares.getNome() == null || this.infocomplementares.getNome().equals("")) {
					this.infocomplementares
							.setNome(rs.getString("RAZAO_SOCIAL") == null ? "" : rs.getString("RAZAO_SOCIAL"));
					this.infocomplementares.setCpfcnpj(s.getCnpj());
				}

				ind++;

			}

			/* verifica se vai ser necessário colocar paginacao na tela */

			// if ( ind <Integer.parseInt(mx.getQtdpesq()) ){
			//
			// this.setPaginasocios(false);
			//
			//
			// }else{
			//
			// this.setPaginasocios(true);
			//
			// }

			paginasociedadesAnt = false;
			paginasociedadesProx = false;
			if (!(mx.getPaginaSocios().equals(1)) && this.sociedades.size() > 0) {
				this.setPaginasociosAnt(true);
			}
			if (this.sociedades.size() > Integer.valueOf(mx.getQtdpesq())) {
				this.setPaginasociedadesProx(true);
				this.sociedades.remove(this.sociedades.size() - 1);
			}

//            //Rodrigo Almeida
//            if (mx.getPaginaSocios().equals(1)  &&  this.socios.size() > 0) {
//                paginasociedadesAnt=true;
//                paginasociedadesProx=true;
//            }
//

		} catch (SQLException e) {

//            logger.error(e.getMessage() + " - CPF/CNPJ: " + sCpf + sCNPJ + " - RazaoSocial: " + sRazaoSocial + " "    + Consulta);
			achou = false;
		} finally {
			if (requestFromViewSingleButton) {
				setPossuiConexao(false);
			}

			if (rs != null)
				rs.close();
			if (stmtN != null)
				stmtN.close();

			releaseConnection();
		}

		if (id > 0) {

			achou = true;

		} else {

			achou = false;

		}
		return achou;

	}

	public Boolean processaConsultaSociosEnder(SqlToBind consulta, int tipopesquisa, LoginMBean mx)
			throws SQLException {

		/*
		 * Método de pesquisa Sociedades
		 *
		 * By SMarcio em 05/11/2013
		 */

		java.sql.PreparedStatement stmtN = null;
		ResultSet rs = null;
		int ind = 0;
		String aux = "";
		String anobase = "";
		int atualizacaoTel = 0;
		int atualizacaoBase = 0;
		String razaosocial = "";
		String celular = "";
		String dtn = "";
		String dts = "";
		String ets = "";
		String areaterreno = "";
		String basecalc = "";
		String diavencimento = "";
		String fracaoideal = "";
		String daincl = "";
		String dalice = "";
		String sql = "";
		String sexo = "";
		String dtabertura = "";
		String cpfcnpj = "";
		String cpfcnpjant = "";
		String ramoatividade = "";
		Boolean achou = false;
		try {

			stmtN = this.getConnection().prepareStatement(consulta.getSql());

			for (int i = 0; i <= consulta.getBinds().size() - 1; i++)
				stmtN.setString(i + 1, consulta.getBinds().get(i));

			rs = stmtN.executeQuery();
			ind = 0;
			int id = 0;
			Socios s;
			Telefone t;

			if (tipopesquisa == 1) {

				this.socios = new ArrayList<Socios>();

				// this.telefone = new ArrayList<Telefone>();

			}

			while (rs != null && rs.next()) {
				id++;
				s = new Socios();
				t = new Telefone();
				/* Pesquisa Socios ConfirmeOnLine New */

				s.setCnpj(this.Filtra(rs.getString("CNPJ") == null ? "" : rs.getString("CNPJ")));
				s.setCpf(this.Filtra(rs.getString("CNPJ") == null ? "" : rs.getString("CNPJ")));
				s.setNome(this.Filtra(rs.getString("RAZAO_SOCIAL") == null ? "" : rs.getString("RAZAO_SOCIAL")));
				s.setCargo(this.Filtra(rs.getString("CARGO") == null ? "" : rs.getString("CARGO")));
				s.setParticipacao(this.Filtra(rs.getString("PARTIC") == null ? "" : rs.getString("PARTIC")));
				s.setDt_entrada(rs.getString("ENTRADA") == null ? "" : rs.getString("ENTRADA"));
				this.socios.add(s);
				this.infocomplementares
						.setNome(rs.getString("RAZAO_SOCIAL") == null ? "" : rs.getString("RAZAO_SOCIAL"));
				this.infocomplementares.setCpfcnpj(s.getCnpj());
				this.infocomplementares
						.setEndereco(this.Filtra(rs.getString("ENDERECO") == null ? "" : rs.getString("ENDERECO")));
				this.infocomplementares
						.setNumero(this.Filtra(rs.getString("NUMERO") == null ? "" : rs.getString("NUMERO")));
				this.infocomplementares.setComplemento(
						this.Filtra(rs.getString("COMPLEMENTO") == null ? "" : rs.getString("COMPLEMENTO")));
				this.infocomplementares
						.setBairro(this.Filtra(rs.getString("BAIRRO") == null ? "" : rs.getString("BAIRRO")));
				this.infocomplementares
						.setCidade(this.Filtra(rs.getString("CIDADE") == null ? "" : rs.getString("CIDADE")));
				this.infocomplementares.setUf(this.Filtra(rs.getString("UF") == null ? "" : rs.getString("UF")));
				String birthday = dao.findBirthday(s.getCpf());
				Integer idade = Utils.calculaIdade(birthday);
				this.infocomplementares.setDtnasc(birthday != null ? birthday + " - " + idade + " anos." : null);
				this.setLabel1("Dt. Fundação:");
				this.infocomplementares.setSigno(this.Filtra(rs.getString("NOME") == null ? "" : rs.getString("NOME")));
				this.setLabel2("Nome Fantasia:");
				this.infocomplementares.setSexo(
						this.Filtra(rs.getString("DESC_NATUREZA") == null ? "" : rs.getString("DESC_NATUREZA")));
				this.setLabel3("Natureza:");
				this.infocomplementares
						.setNomemae(this.Filtra(rs.getString("SITUACAO") == null ? "" : rs.getString("SITUACAO")));
				this.setLabel4("Situação:");
				this.setLabel5("");

				// if ( id == 1 ){
				//
				// t.setEoprimeiro(true);
				//
				// }
				t.setNumero(this.Filtra(rs.getString("NUMERO") == null ? "" : rs.getString("NUMERO")));
				t.setEndereco(this.Filtra(rs.getString("ENDERECO") == null ? "" : rs.getString("ENDERECO")));
				t.setNumeroTelefone("-----------");
				t.setComplemento(this.Filtra(rs.getString("COMPLEMENTO") == null ? "" : rs.getString("COMPLEMENTO")));
				t.setBairro(this.Filtra(rs.getString("BAIRRO") == null ? "" : rs.getString("BAIRRO")));
				t.setCidade(this.Filtra(rs.getString("CIDADE") == null ? "" : rs.getString("CIDADE")));
				t.setUf(this.Filtra(rs.getString("UF") == null ? "" : rs.getString("UF")));
				t.setCpfcnpj(this.Filtra(rs.getString("CNPJ") == null ? "" : rs.getString("CNPJ")));
				t.setId(getNextIdOnTelefones());
				this.telefone.add(t);
				ind++;

			}
			this.telefone.get(0).setEoprimeiro(true);

			/* verifica se vai ser necessário colocar paginacao na tela */

			if (ind < Integer.parseInt(mx.getQtdpesq())) {

				this.setPaginasocios(false);

			} else {

				this.setPaginasocios(true);

			}

			if (id > 0) {

				achou = true;

			} else {

				achou = false;

			}

		} catch (SQLException e) {
			logger.error("Erro no metodo processaConsultaSociosEnder: " + e.getMessage());
			achou = false;
		} finally {
			releaseConnection();
			if (stmtN != null)
				stmtN.close();
			if (rs != null)
				rs.close();
		}
		return achou;

	}

	public Boolean processaConsultaEnderecosComerciais(int tipopesquisa, LoginMBean mx, Integer paginaInicial,
			Integer paginaFinal) {

		/*
		 * Método de pesquisa Sociedades
		 *
		 * By SMarcio em 05/11/2013
		 */

		CallableStatement stmt;
		ResultSet rs;
		int ind = 0;
		String aux = "";
		String anobase = "";
		int atualizacaoTel = 0;
		int atualizacaoBase = 0;
		String razaosocial = "";
		String celular = "";
		String dtn = "";
		String dts = "";
		String ets = "";
		String areaterreno = "";
		String basecalc = "";
		String diavencimento = "";
		String fracaoideal = "";
		String daincl = "";
		String dalice = "";
		String sql = "";
		String sexo = "";
		String dtabertura = "";
		String cpfcnpj = "";
		String cpfcnpjant = "";
		String ramoatividade = "";
		Boolean achou = false;
		Connection conn = this.getConnection();
		try {

			int i = 1;
			/* trecho tomcat */

			// conn=((DelegatingConnection)this.getConnection()).getInnermostDelegate();
			stmt = conn.prepareCall("BEGIN CONFIRME_ENDERECOS_COMERCIAIS(?,?,?,?,?,?,?); END;");

			stmt.setString(i++, this.infocomplementares.getCpfcnpj());
			stmt.setString(i++, this.infocomplementares.getNome() == null ? "" : this.infocomplementares.getNome());
			stmt.setString(i++, mx.getUsuario().getLogin());
			stmt.setInt(i++, paginaInicial);
			stmt.setInt(i++, paginaFinal);
			stmt.setInt(i++, br.com.confirmeonline.util.SQLConstantes.QTD_MAX_ENDERECOS_COMERCIAIS);
			stmt.registerOutParameter(i, OracleTypes.CURSOR);
			stmt.execute();
			rs = (ResultSet) stmt.getObject(i);

			/* trecho weblogic */
			/*
			 * conn = this.getConnection(); stmt = conn.
			 * prepareCall("BEGIN CONFIRME_ENDERECOS_COMERCIAIS(?,?,?,?,?,?,?); END;" );
			 * stmt.setString(i++, this.infocomplementares.getCpfcnpj());
			 * stmt.setString(i++, this.infocomplementares.getNome()==null?"":this.
			 * infocomplementares.getNome()); stmt.setString(i++,
			 * mx.getUsuario().getLogin()); stmt.setInt(i++, paginaInicial);
			 * stmt.setInt(i++, paginaFinal); stmt.setInt(i++,
			 * br.com.confirmeonline.util.SQLConstantes. QTD_MAX_ENDERECOS_COMERCIAIS);
			 * stmt.registerOutParameter(i, OracleTypes.CURSOR); stmt.execute(); rs =
			 * (ResultSet) stmt.getObject(i);
			 */
			ind = 0;
			int id = 0;
			EnderecoComercial e;

			if (tipopesquisa == 1) {

				this.enderecosComerciais = new ArrayList<EnderecoComercial>();

				// this.telefone = new ArrayList<Telefone>();

			}

			while (rs != null && rs.next()) {
				id++;
				e = new EnderecoComercial();
				e.setCpfcgc(rs.getString("CPFCGC") == null ? "" : rs.getString("CPFCGC"));
				e.setRazaoSocial(rs.getString("RAZAO_SOCIAL") == null ? "" : rs.getString("RAZAO_SOCIAL"));
				e.setFantasia(rs.getString("FANTASIA") == null ? "" : rs.getString("FANTASIA"));
				Date admissao = rs.getDate("ADMISSAO");
				e.setAdmissao(new SimpleDateFormat("yyyy").format(admissao));
				e.setEndereco(rs.getString("ENDERECO") == null ? "" : rs.getString("ENDERECO"));
				e.setComplemento(rs.getString("COMPLEMENTO") == null ? "" : rs.getString("COMPLEMENTO"));
				e.setBairro(rs.getString("BAIRRO") == null ? "" : rs.getString("BAIRRO"));
				e.setCidade(rs.getString("CIDADE") == null ? "" : rs.getString("CIDADE"));
				e.setCep(rs.getString("CEP") == null ? "" : rs.getString("CEP"));
				e.setNumero(rs.getString("NUMERO") == null ? "" : rs.getString("NUMERO"));
				e.setUf(rs.getString("UF") == null ? "" : rs.getString("UF"));

				this.enderecosComerciais.add(e);

				ind++;

			}

			stmt.close();
			rs.close();
			conn.close();

			/* verifica se vai ser necessário colocar paginacao na tela */

			if (ind < Integer.parseInt(mx.getQtdpesq())) {

				this.setPaginaEnderecosComerciais(false);

			} else {

				this.setPaginaEnderecosComerciais(true);

			}

			if (id > 0) {

				achou = true;

			} else {

				achou = false;

			}

		} catch (Exception e) {

			achou = false;
		} finally {
			releaseConnection();
		}
		return achou;

	}

	public Boolean processaConsultaObito(String Consulta, LoginMBean mx) {

		/*
		 * Método de pesquisa óbitos
		 *
		 * By SMarcio em 05/11/2013
		 */
		Boolean achou = false;
		java.sql.Statement stmtN;
		ResultSet rs;
		int ind = 0;
		String aux = "";
		String anobase = "";
		int atualizacaoTel = 0;
		int atualizacaoBase = 0;
		String nome = "";
		String celular = "";
		String dtn = "";
		String dts = "";
		String ets = "";
		String areaterreno = "";
		String basecalc = "";
		String diavencimento = "";
		String fracaoideal = "";
		String daincl = "";
		String dalice = "";
		String sql = "";
		String sexo = "";
		String dtabertura = "";
		String cpfcnpj = "";
		Boolean requestFromViewSingleButton = false;

		try {

			stmtN = this.getConnection().createStatement();
			if (!possuiConexao) {
				setPossuiConexao(true);
				requestFromViewSingleButton = true;
			}
			rs = stmtN.executeQuery(Consulta);
			ind = 0;
			int id = 1;
			String nasc = "";
			String emails = "";
			String dtobito = "";
			this.obito = new Obito();
			while (rs != null && rs.next()) {

				/* Pesquisa Obtito */

				/*
				 * QUERY
				 *
				 * NU_LIVRO -> NUMERO DO LIVRO NU_FOLHA -> NUMERO DA FOLHA NU_TERMO -> NUMERO DO
				 * TERMO DT_LAVRAT -> DATA DE REGISTRO DO ÓBITO NU_NB -> ??? NM_FALECIDO -> NOME
				 * DO FALECIDO NM_MAE_FALECIDO -> NOME DA MAE DO FALECIDO DT_NASC -> DATA DE
				 * NASCIMENTO DO FALECIDO DT_OBITO -> DATA DO ÓBTIO NU_CPF -> NUMERO DO CPF DO
				 * FALECIDO NIT -> ??? CS_IDENT -> ??? ID_CARTORIO -> CNPJ DO CARTORIO
				 * NM_CARTORIO -> NOME DO CARTORIO ENDERECO_CARTORIO -> ENDERECO DO CARTORIO
				 * CIDADE -> CIDADE DO CARTORIO NU_CEP -> CEP DO CARTORIO BAIRRO -> BAIRRO DO
				 * CARTORIO
				 */

				this.obito = new Obito();
				nome = this.Filtra(rs.getString("NM_FALECIDO") == null ? "" : rs.getString("NM_FALECIDO"));
				cpfcnpj = this.Filtra(rs.getString("NU_CPF") == null ? "" : rs.getString("NU_CPF"));
				obito.setNucpf(cpfcnpj);
				obito.setNmfalecido(nome);
				String birthday = dao.findBirthday(cpfcnpj);
				Integer idade = Utils.calculaIdade(birthday);
				obito.setDtnasc(birthday != null ? birthday + " - " + idade + " anos." : null);
				// obito.setDtnasc(rs.getString("DT_NASC") == null ? "" :
				// rs.getString("DT_NASC"));
				obito.setNmmaefalecido(rs.getString("NM_MAE_FALECIDO") == null ? "" : rs.getString("NM_MAE_FALECIDO"));
				obito.setDtobito(this.Filtra(rs.getString("DT_OBITO") == null ? "" : rs.getString("DT_OBITO")));
				obito.setNmcartorio(
						this.Filtra(rs.getString("NM_CARTORIO") == null ? "" : rs.getString("NM_CARTORIO")));
				obito.setNufolha(this.Filtra(rs.getString("NU_FOLHA") == null ? "" : rs.getString("NU_FOLHA")));
				obito.setNulivro(this.Filtra(rs.getString("NU_LIVRO") == null ? "" : rs.getString("NU_LIVRO")));
				obito.setNunb(this.Filtra(rs.getString("NU_NB") == null ? "" : rs.getString("NU_NB")));
				obito.setNutermo(this.Filtra(rs.getString("NU_TERMO") == null ? "" : rs.getString("NU_TERMO")));
				obito.setIdcartorio(
						this.Filtra(rs.getString("ID_CARTORIO") == null ? "" : rs.getString("ID_CARTORIO")));
				obito.setDtlavral(this.Filtra(rs.getString("DT_LAVRAT") == null ? "" : rs.getString("DT_LAVRAT")));
				obito.setCidade(this.Filtra(rs.getString("CIDADE") == null ? "" : rs.getString("CIDADE")));
				obito.setEnderecoCartorio(this
						.Filtra(rs.getString("ENDERECO_CARTORIO") == null ? "" : rs.getString("ENDERECO_CARTORIO")));
				obito.setCep(this.Filtra(rs.getString("NU_CEP") == null ? "" : rs.getString("NU_CEP")));
				obito.setBairro(this.Filtra(rs.getString("BAIRRO") == null ? "" : rs.getString("BAIRRO")));
				obito.setCsident(this.Filtra(rs.getString("CS_IDENT") == null ? "" : rs.getString("CS_IDENT")));
				obito.setNit(this.Filtra(rs.getString("NIT") == null ? "" : rs.getString("NIT")));
				ind++;

				achou = true;

			}
			stmtN.close();
			rs.close();

		} catch (SQLException e) {
			achou = false;
		} finally {
			if (requestFromViewSingleButton) {
				setPossuiConexao(false);
			}
			releaseConnection();
		}

		return achou;

	}

	public Boolean processaEmail(Integer comando, LoginMBean mx) {

		/*
		 * Método de pesquisa Emails
		 *
		 * By SMarcio em 05/11/2013
		 */

		Boolean achou = false;
		String sql = "";
		String emails = "";
		int ind = 0;
		Emails em;
		Integer pagina;
		Integer paginaFinal;
		Integer paginaInicial;
		Integer qtdpesq;
		Boolean requestFromViewSingleButton = false;

		this.emails = new ArrayList<Emails>();

		try {
			qtdpesq = Integer.parseInt(mx.getQtdpesq());
			pagina = mx.getPaginaEmail();

			if (qtdpesq < 1) {

				qtdpesq = 1;

			}
			if (qtdpesq > 100) {

				qtdpesq = 100;
			}
			/* Avança registro */

			/*
			 * Se o comando 0 for passado, sempre vai iniciar a pesquisa do inicio
			 */

			if (comando == 1) {

				pagina = pagina + 1;

			}

			/* Volta registro */
			if (comando == 2) {

				pagina = pagina - 1;
			}

			paginaFinal = qtdpesq * pagina;
			paginaInicial = (paginaFinal - qtdpesq) + 1;

			if (pagina == 1 || pagina < 1) {

				paginaFinal = qtdpesq;
				paginaInicial = 1;
				pagina = 1;
			}

			/* 250 e hardcode e o maximo de registros que pesquisamos */
			if (pagina > (250 / qtdpesq)) {

				pagina = 250 / qtdpesq;
				paginaFinal = 250;
				paginaInicial = (paginaFinal - qtdpesq) + 1;
			}

			if (comando == 0) {

				pagina = 1;
				paginaFinal = qtdpesq;
				paginaInicial = 1;

			}
			paginaFinal += 1;

			mx.setPaginaEmail(pagina);
			sql = "SELECT * FROM ( ";
			sql = sql + " SELECT PAGINA.*,ROWNUM PAGINA_RN FROM ( ";
			sql = sql + " SELECT * FROM ( ";
			sql = sql + " SELECT EM_NM_EMAIL FROM DM_EMAIL WHERE CPF_CNPJ = '" + mx.getPessoaSite().getCpfcnpj()
					+ "' AND ( ROWNUM <= " + br.com.confirmeonline.util.SQLConstantes.QTD_MAX_PESQUISA
					+ " )  ORDER BY ID_VALIDADO ASC";
			sql = sql + "  )  ) PAGINA   ) WHERE ( PAGINA_RN >= '" + paginaInicial + "' AND PAGINA_RN <= '"
					+ paginaFinal + "' ) ";

			java.sql.Statement stmtN = this.getConnection().createStatement();
			if (!possuiConexao) {
				setPossuiConexao(true);
				requestFromViewSingleButton = true;
			}
			ResultSet rs = stmtN.executeQuery(sql);

			while (rs != null && rs.next()) {

				ind++;
				em = new Emails();
				em.setCpfcnpj(mx.getPessoaSite().getCpfcnpj());
				em.setEmail(rs.getString("EM_NM_EMAIL"));
				em.setId(ind);
				this.emails.add(em);
				achou = true;
			}

			rs.close();
			stmtN.close();
			/* verifica se vai ser necessário colocar paginacao na tela */

			// if ( pagina == 1 ) {
			//
			// if ( ind <Integer.parseInt(mx.getQtdpesq()) ){
			//
			// this.setPaginaemail(false);
			//
			//
			// }else{
			//
			// this.setPaginaemail(true);
			//
			// }
			//
			// }

			this.setPaginaemailAnt(false);
			this.setPaginaemailProx(false);
			if (!(mx.getPaginaEmail().equals(1)) && this.emails.size() > 0) {
				this.setPaginaemailAnt(true);
			}
			if (this.emails.size() > Integer.valueOf(mx.getQtdpesq())) {
				this.setPaginaemailProx(true);
				this.emails.remove(this.emails.size() - 1);
			}
		} catch (Exception e) {
			achou = false;
		} finally {
			if (requestFromViewSingleButton) {
				setPossuiConexao(false);
			}
			releaseConnection();
		}
		return achou;
	}

	public Boolean processaConsultaTelRef(LoginMBean mb, Integer comando, Integer indice) {

		/*
		 * Método de pesquisa telefones Referência
		 *
		 * By SMarcio em 05/11/2013
		 */

		StringBuilder sql = new StringBuilder();
		Boolean ok = false;
		Integer paginaFinal = 0;
		Integer paginaInicial = 0;
		Integer qtdpesq = 0;
		Integer pagina = 0;
		try {

			qtdpesq = Integer.parseInt(mb.getQtdpesq());
			pagina = mb.getPaginaTelRef();

			if (qtdpesq < 1) {

				qtdpesq = 1;

			}
			if (qtdpesq > 100) {

				qtdpesq = 100;
			}
			/* Avança registro */

			/*
			 * Se o comando 0 for passado, sempre vai iniciar a pesquisa do inicio
			 */

			if (comando == 1) {

				pagina = pagina + 1;

			}

			/* Volta registro */
			if (comando == 2) {

				pagina = pagina - 1;
			}

			paginaFinal = qtdpesq * pagina;
			paginaInicial = (paginaFinal - qtdpesq) + 1;

			if (pagina == 1 || pagina < 1) {

				paginaFinal = qtdpesq;
				paginaInicial = 1;
				pagina = 1;
			}

			/* 250 e hardcode e o maximo de registros que pesquisamos */
			if (pagina > (250 / qtdpesq)) {

				pagina = 250 / qtdpesq;
				paginaFinal = 250;
				paginaInicial = (paginaFinal - qtdpesq) + 1;
			}

			if (comando == 0) {

				pagina = 1;
				paginaFinal = qtdpesq;
				paginaInicial = 1;

			}

			mb.setPaginaTelRef(pagina);

			sql.append("SELECT * FROM ( ");
			sql.append(" SELECT PAGINA.*,ROWNUM PAGINA_RN FROM ( ");
			sql.append(
					" SELECT (SELECT STATUS FROM TELEFONES_STATUS WHERE TELEFONE = T.TELEFONE AND ROWNUM <= 1) AS STATUS_LINHA, ");
			sql.append(
					" (CASE WHEN (SELECT FLAG FROM TBL_WHATSAPP T2 WHERE T2.TELEFONE = T.TELEFONE AND ROWNUM <= 1) = 1 THEN 1 ELSE 0 END) AS WHATSAPP, ");
			sql.append(
					" (SELECT IS_DATE_CONFIRME(DT_OBITO) FROM OBITO WHERE NU_CPF = CPFCGC AND ROWNUM<=1 ) AS OBITO, ");
			sql.append(
					" (SELECT DT_ABERTURA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS DT_ABERTURA, ");
			sql.append(" (SELECT SITUACAO FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS SITUACAO,");
			sql.append(" (SELECT FANTASIA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS FANTASIA, ");
			sql.append(" (SELECT DESC_NATUREZA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS NATUREZA,");
			sql.append(
					" I.NOME,T.PROPRIETARIO AS PROPRIETARIO,T.ATUAL,T.TELEFONE,T.ENDERECO AS ENDERECO,T.NUMERO AS NUMERO,T.COMPLEMENTO,T.BAIRRO,T.CEP,T.CIDADE,T.UF,T.CPFCGC AS CPFCNPJ,");
			sql.append(
					" i.CPF_CONJUGE AS CPF_CONJUGE,PEGA_NOME_INFO_COMP(I.CPF_CONJUGE) AS NOME_CONJUGE,I.NOME_MAE AS MAE,TO_CHAR(I.DATA_NASC,'DD/MM/YYYY') AS NASC,' ' AS TITULO,T.OPERADORA,TO_CHAR(T.DT_INSTALACAO,'YYYY-MM-DD') AS DT_INSTALACAO, I.SIGNO AS SIGNO ");
			sql.append(" FROM DBCRED.TELEFONES T, DBCRED.INFO_COMPLEMENTARES I , DBCRED.TELEFONES_REFERENCIA R  ");
			sql.append(" WHERE  ");
			sql.append(" T.PROPRIETARIO IS NOT NULL      AND  ");
			sql.append(" T.TELEFONE = R.TELEFONE         AND ");
			sql.append(" T.CPFCGC = I.CPFCNPJ(+)         AND ");
			sql.append(" T.CPFCGC <> '" + mb.getPessoaSite().getCpfcnpj() + "'       AND  ");
			sql.append(" R.CPFCNPJ = '" + mb.getPessoaSite().getCpfcnpj() + "' AND ( ROWNUM <= "
					+ br.com.confirmeonline.util.SQLConstantes.QTD_MAX_PESQUISA + " ) ORDER BY TO_NUMBER(ATUAL) DESC ");
			sql.append(" ) PAGINA ");
			sql.append(" ) WHERE  ( PAGINA_RN >=  " + paginaInicial + "  AND  PAGINA_RN <= " + paginaFinal
					+ " ) ORDER BY TO_NUMBER(ATUAL) DESC, WHATSAPP DESC ");

			ok = processaConsultaTelRefAux(sql.toString(), 1, mb);
			return ok;

		} catch (Exception e) {

			return false;

		}

	}

	public Boolean processaConsultaTelRefAux(String Consulta, int tipopesquisa, LoginMBean mx) {
		/*
		 * Método de pesquisa Telefones Referência auxiliar
		 *
		 * By SMarcio em 05/11/2013
		 */

		java.sql.Statement stmtN;
		ResultSet rs;
		int ind = 0;
		String aux = "";
		String anobase = "";
		int atualizacaoTel = 0;
		int atualizacaoBase = 0;
		String nome = "";
		String celular = "";
		String dtn = "";
		String dts = "";
		String ets = "";
		String areaterreno = "";
		String basecalc = "";
		String diavencimento = "";
		String fracaoideal = "";
		String daincl = "";
		String dalice = "";
		String sql = "";
		String sexo = "";
		String dtabertura = "";
		String cpfcnpj = "";
		String cpfcnpjant = "";
		Connection conn = this.getConnection();
		Boolean achou = false;
		Boolean requestFromViewSingleButton = false;
		if (!possuiConexao) {
			setPossuiConexao(true);
			requestFromViewSingleButton = true;
		}
		try {

			stmtN = conn.createStatement();
			rs = stmtN.executeQuery(Consulta);
			ind = 0;
			int id = 0;
			String nasc = "";
			String emails = "";
			TelefonesReferencia t;
			String dtobito = "";
			Infocomplementares info;

			if (tipopesquisa == 1) {

				this.telefoneReferencia = new ArrayList<TelefonesReferencia>();

			}

			while (rs != null && rs.next()) {
				id++;
				t = new TelefonesReferencia();
				info = new Infocomplementares();

				/* Telefones Referência Detalhamento */

				cpfcnpj = this.Filtra(rs.getString("CPFCNPJ") == null ? "" : rs.getString("CPFCNPJ"));

				if (!cpfcnpj.equals(cpfcnpjant)) {

					t.setEoprimeiro(true);
					cpfcnpjant = cpfcnpj;
					id = 1;
				}
				nome = this.Filtra(rs.getString("NOME") == null ? "" : rs.getString("NOME"));

				if (nome.equals("")) {

					nome = this.Filtra(rs.getString("PROPRIETARIO") == null ? "" : rs.getString("PROPRIETARIO"));

				}

				info.setNome(nome);

				sexo = this.Filtra(GetSexo(rs.getString("PROPRIETARIO") == null ? "" : rs.getString("PROPRIETARIO"),
						rs.getString("CPFCNPJ") == null ? "" : rs.getString("CPFCNPJ")));

				if (sexo.equals("M")) {

					sexo = "MASCULINO";

				}
				if (sexo.equals("F")) {

					sexo = "FEMININO";

				}
				String birthday = dao.findBirthday(cpfcnpj);
				Integer idade = Utils.calculaIdade(birthday);
				info.setDtnasc(birthday != null ? birthday + " - " + idade + " anos." : null);

				if (cpfcnpj.length() < 14) {
					t.setLabel1("Dt. Nascimento:");

					info.setSigno(Utils.findSigno(info.getDtnasc(), "dd/MM/yyyy"));
					t.setLabel2("Signo:");

					info.setSexo(sexo);
					t.setLabel3("Sexo:");

					info.setNomemae(this.Filtra(rs.getString("MAE") == null ? "" : rs.getString("MAE")));
					t.setLabel4("Nome da Mãe:");

					info.setTituloEleitor(this.getTituloEleitoral(cpfcnpj, this.getConnection()));
					t.setLabel5("Título de Eleitor:");
				}

				if (cpfcnpj.length() == 14) {
					t.setLabel1("Dt. Fundação:");

					info.setSigno(this.Filtra(rs.getString("FANTASIA") == null ? "" : rs.getString("FANTASIA")));
					t.setLabel2("Nome Fantasia:");

					info.setSexo(this.Filtra(rs.getString("NATUREZA") == null ? "" : rs.getString("NATUREZA")));
					t.setLabel3("Natureza:");

					info.setNomemae(this.Filtra(rs.getString("SITUACAO") == null ? "" : rs.getString("SITUACAO")));
					t.setLabel4("Situação:");
					t.setLabel5("");

				}
				dtobito = this.Filtra(rs.getString("OBITO") == null ? "" : rs.getString("OBITO"));

				dtobito = ObitoDao.findDataObito(mx, cpfcnpj, nome, conn);
				info.setDtobito(dtobito);
				info.setCpfcnpj(this.Filtra(rs.getString("CPFCNPJ") == null ? "" : rs.getString("CPFCNPJ")));
				t.setInfotelefone(info);

				t.setId(id);
				t.setProprietario(
						this.Filtra(rs.getString("PROPRIETARIO") == null ? "" : rs.getString("PROPRIETARIO")));
				t.setEndereco(this.Filtra(rs.getString("ENDERECO") == null ? "" : rs.getString("ENDERECO")));
				t.setNumero(this.Filtra(rs.getString("NUMERO") == null ? "" : rs.getString("NUMERO")));
				t.setComplemento(this.Filtra(rs.getString("COMPLEMENTO") == null ? "" : rs.getString("COMPLEMENTO")));
				t.setBairro(this.Filtra(rs.getString("BAIRRO") == null ? "" : rs.getString("BAIRRO")));
				t.setCidade(this.Filtra(rs.getString("CIDADE") == null ? "" : rs.getString("CIDADE")));
				t.setCep(this.Filtra(rs.getString("CEP") == null ? "" : rs.getString("CEP")));
				t.setUf(this.Filtra(rs.getString("UF") == null ? "" : rs.getString("UF")));
				t.setCpfcnpj(cpfcnpj);
				t.setNumeroTelefone(
						this.Filtra(rs.getString("TELEFONE") == null ? "-----------" : rs.getString("TELEFONE")));

				t.setStatusLinha(this.Filtra(rs.getString("STATUS_LINHA") == null ? "" : rs.getString("STATUS_LINHA")));
				t.setAtual(this.Filtra(rs.getString("ATUAL") == null ? "" : rs.getString("ATUAL")));

				/*
				 * Verifica se o cliente possui direito a base que ele esta consultando, se não
				 * mostra alguns dados mas os endereços e telefones não mostra
				 */

				if (Conexao.GetSerial(conn, mx.getUsuario().getLogin()).indexOf(t.getUf()) != -1) {

					t.setNumeroTelefone(
							this.Filtra(rs.getString("TELEFONE") == null ? "-----------" : rs.getString("TELEFONE")));
					t.setEndereco(this.Filtra(rs.getString("ENDERECO") == null ? "" : rs.getString("ENDERECO")));
					t.setNumero(this.Filtra(rs.getString("NUMERO") == null ? "" : rs.getString("NUMERO")));
					t.setComplemento(
							this.Filtra(rs.getString("COMPLEMENTO") == null ? "" : rs.getString("COMPLEMENTO")));
					t.setBairro(this.Filtra(rs.getString("BAIRRO") == null ? "" : rs.getString("BAIRRO")));
					t.setCidade(this.Filtra(rs.getString("CIDADE") == null ? "" : rs.getString("CIDADE")));
					t.setCep(this.Filtra(rs.getString("CEP") == null ? "" : rs.getString("CEP")));
					t.setProcon(this.proconSP(t.getNumeroTelefone(), conn));
					if (t.getNumeroTelefone().length() < 9) {

						t.setNumeroTelefone("-----------");

					}

				} else {

					t.setNumeroTelefone("--Estado não contratado.");
					t.setEndereco("Estado não contratado ");
					t.setNumero("-");
					t.setComplemento("-");
					t.setBairro("-");
					t.setCidade("-");
					t.setCep("-");
					t.setProcon("-");

				}
				t.setRatingTelefone(t.getAtual(), getUseStatus());
				t.setWhatsApp(rs.getString("WHATSAPP"));
				this.telefoneReferencia.add(t);

				ind++;

			}
			stmtN.close();
			rs.close();
			/* verifica se vai ser necessário colocar paginacao na tela */

			// if ( mx.getPaginaTelRef() == 1){
			//
			// if ( ind < Integer.parseInt(mx.getQtdpesq()) ){
			//
			// this.setPaginatelefonesreferencia(false);
			//
			// }else{
			//
			// this.setPaginatelefonesreferencia(true);
			//
			// }
			// }
			this.setPaginatelefonesreferenciaAnt(false);
			this.setPaginatelefonesreferenciaProx(false);
			if (!(mx.getPaginaTelRef().equals(1)) && (this.telefoneReferencia.size() > 0)) {
				this.setPaginatelefonesreferenciaAnt(true);
			}
			if (this.telefoneReferencia.size() > Integer.valueOf(mx.getQtdpesq())) {
				this.setPaginatelefonesreferenciaProx(true);
				this.telefoneReferencia.remove(this.telefoneReferencia.size() - 1);
			}
		} catch (SQLException e) {

			achou = false;
		} finally {
			if (requestFromViewSingleButton) {
				setPossuiConexao(false);
			}
			releaseConnection();
		}
		if (ind > 0) {
			achou = true;
		} else {
			achou = false;
		}
		return achou;
	}

	public Boolean processaConsultaImovel(String Consulta, int tipopesquisa, LoginMBean mx) {
		/*
		 * Método de pesquisa Imoveis
		 *
		 * By SMarcio em 05/11/2013
		 */

		java.sql.Statement stmtN;
		ResultSet rs;
		int ind = 0;
		String sql = "";
		String cpfcnpj = "";
		String nome = "";
		String endereco = "";
		String cep = "";
		String setor = "";
		String quadra = "";
		String lote = "";
		String codigo_log = "";
		String area_terreno = "";
		String area_construida = "";
		String ano_construcao = "";
		String base_calculo = "";
		String dia_vencimento = "";
		String testada = "";
		String fracao_ideal = "";
		String inscricao = "";
		String cpfcnpjant = "";
		Boolean achou = false;
		Boolean requestFromViewSingleButton = false;
		try {

			stmtN = this.getConnection().createStatement();
			if (!possuiConexao) {
				setPossuiConexao(true);
				requestFromViewSingleButton = true;
			}
			rs = stmtN.executeQuery(Consulta);
			ind = 0;
			int id = 0;
			Imoveis t;

			if (tipopesquisa == 1) {

				this.imoveis = new ArrayList<Imoveis>();

			}

			while (rs != null && rs.next()) {
				id++;
				t = new Imoveis();

				/* Telefones Referéncia Detalhamento */

				cpfcnpj = this.Filtra(rs.getString("CPF") == null ? "" : rs.getString("CPF"));

				if (!cpfcnpj.equals(cpfcnpjant)) {

					t.setEoprimeiro(true);
					cpfcnpjant = cpfcnpj;
					id = 1;
				}
				nome = this.Filtra(rs.getString("NOME") == null ? "" : rs.getString("NOME"));
				t.setId(id);
				t.setNome(this.Filtra(nome));
				t.setEndereco(this.Filtra(rs.getString("ENDERECO") == null ? "" : rs.getString("ENDERECO")));
				t.setCep(this.Filtra(rs.getString("CEP") == null ? "" : rs.getString("CEP")));
				t.setCpfcnpj(cpfcnpj);
				t.setAno_construcao(
						this.Filtra(rs.getString("ANO_CONSTRUCAO") == null ? "" : rs.getString("ANO_CONSTRUCAO")));
				t.setArea_construida(
						this.Filtra(rs.getString("AREA_CONSTRUIDA") == null ? "" : rs.getString("AREA_CONSTRUIDA")));
				t.setArea_terreno(
						this.Filtra(rs.getString("AREA_TERRENO") == null ? "" : rs.getString("AREA_TERRENO")));
				t.setBase_calculo(
						this.Filtra(rs.getString("ANO_CONSTRUCAO") == null ? "" : rs.getString("ANO_CONSTRUCAO")));
				t.setCodigo_log(
						this.Filtra(rs.getString("ANO_CONSTRUCAO") == null ? "" : rs.getString("ANO_CONSTRUCAO")));
				t.setDia_vencimento(rs.getString("DIA_VENCIMENTO") == null ? "" : rs.getString("DIA_VENCIMENTO"));
				t.setFracao_ideal(
						this.Filtra(rs.getString("FRACAO_IDEAL") == null ? "" : rs.getString("FRACAO_IDEAL")));
				t.setInscricao(this.Filtra(rs.getString("INSCRICAO") == null ? "" : rs.getString("INSCRICAO")));
				t.setLote(this.Filtra(rs.getString("LOTE") == null ? "" : rs.getString("LOTE")));
				t.setQuadra(this.Filtra(rs.getString("QUADRA") == null ? "" : rs.getString("QUADRA")));
				t.setSetor(this.Filtra(rs.getString("SETOR") == null ? "" : rs.getString("SETOR")));
				t.setTestada(this.Filtra(rs.getString("TESTADA") == null ? "" : rs.getString("TESTADA")));
				this.imoveis.add(t);
				ind++;

			}
			stmtN.close();
			rs.close();
			/* verifica se vai ser necessário colocar paginacao na tela */

			// if ( mx.getPaginaImoveis() == 1 ){
			//
			// if ( ind < Integer.parseInt(mx.getQtdpesq()) ){
			//
			// this.setPaginaimoveis(false);
			//
			// }else{
			//
			// this.setPaginaimoveis(true);
			//
			// }
			// }
			this.setPaginaimoveisAnt(false);
			this.setPaginaimoveisProx(false);
			if (!(mx.getPaginaImoveis().equals(1)) && this.imoveis.size() > 0) {
				this.setPaginaimoveisAnt(true);
			}
			if (this.imoveis.size() > Integer.valueOf(mx.getQtdpesq())) {
				this.setPaginaimoveisProx(true);
				this.imoveis.remove(this.imoveis.size() - 1);
			}
		} catch (SQLException e) {
			achou = false;
		} finally {
			if (requestFromViewSingleButton) {
				setPossuiConexao(false);
			}
			releaseConnection();
		}
		if (ind > 0) {
			achou = true;
		} else {
			achou = false;
		}
		return achou;
	}

	public SqlToBind pesquisaCep(SqlToBind consulta, LoginMBean mx) throws SQLException {

		java.sql.PreparedStatement stmtN = null;
		ResultSet rs = null;
		PesquisaCepUtil resultado = new PesquisaCepUtil();
		int ind = 0;
		String ret = "";
		String ret1 = "";
		String cep1 = "";
		
		SqlToBind resultado2 = new SqlToBind();

		try {

			stmtN = this.getConnection().prepareStatement(consulta.getSql());

			for (int i = 0; i <= consulta.getBinds().size() - 1; i++)
				stmtN.setString(i + 1, consulta.getBinds().get(i));

			rs = stmtN.executeQuery();
			ind = 0;
			while (rs != null && rs.next() && ind < 10) {
				ind = ind + 1;
				if (ind == 1) {

					cep1 = rs.getString("CEP");
					ret = "T.CEP= ? ";
					ret1 = "(T.CEP = ? ";
					resultado.setBind(cep1);
					resultado2.addString(cep1); //Rodrigo Almeida - 19/03/2020
				} else {

					ret1 = ret1 + " OR T.CEP = ?";
					resultado.setBind(rs.getString("CEP"));
					resultado2.addString(rs.getString("CEP"));
				}
			}

			if (ind == 0) {
				ret = "T.CEP= ? ";
				resultado.setBind(String.valueOf(-1));
				resultado2.addString(String.valueOf(-1));
			} else {

				ret = ret1 + ")";

			}
			if (ret.equals(""))
				ret = mx.getPessoaSite().getCep().getCep();

		} catch (SQLException e) {
			logger.error("Erro no metodo pesquisaCep da classe Resposta: " + e.getMessage());
			// ret = " T.CEP ='" + mx.getPessoaSite().getCep().getCep() + "' ";
		} finally {
			releaseConnection();
			if (stmtN != null)
				stmtN.close();
			if (rs != null)
				rs.close();
		}
		resultado.setCompletaSql(ret);
		resultado2.setSql(ret);
		return resultado2;
	}

	public Boolean pesquisaConsultaEnderecoCepNome(int tipopesquisa, LoginMBean mx, int comando, String qtdpesq,
			Integer pagina) {
		/*
		 * Método de pesquisa Multimpla esta pesquisa possui sempre o mesmo tipo de
		 * retorno a mudança esta apenas na query
		 *
		 * Este método usa a classe de vizinhos para mostrar os resultados By SMarcio em
		 * 12/11/2013
		 */

		java.sql.Statement stmtN;
		ResultSet rs;
		int ind = 0;
		String[] armazenados = new String[5];
		Integer posix = 0;
		StringBuilder sql = new StringBuilder();
		StringBuilder sql2 = new StringBuilder();
		StringBuilder sql3 = new StringBuilder();
		StringBuilder sql1 = new StringBuilder();
		SqlToBind resultado = new SqlToBind();
		SqlToBind resultado2 = new SqlToBind();
		String cpfcnpj = "";
		String nome = "";
		String endereco = "";
		String cep = "";
		String setor = "";
		String quadra = "";
		String lote = "";
		String codigo_log = "";
		String area_terreno = "";
		String area_construida = "";
		String ano_construcao = "";
		String base_calculo = "";
		String dia_vencimento = "";
		String testada = "";
		String fracao_ideal = "";
		String inscricao = "";
		String cpfcnpjant = "";
		String logradouro = "";
		SqlToBind cepPesq;
		Integer paginaFinal = 0;
		Integer paginaInicial = 0;
		Boolean ok;
		Connection conn = this.getConnection();
		this.possuiConexao = true;
		/*
		 *
		 * tipopesquisa = 1 - pesquisa por endereço tipopesquisa = 2 - pesquisa por cep
		 * tipopesquisa = 3 - pesquisa por nome
		 *
		 */
		this.vizinhos = new ArrayList<Vizinhos>();

		try {

		} catch (Exception e) {
			logger.error("Erro no metodo pesquisaConsultaEnderecoCepNome da classe Resposta: " + e.getMessage());

		}

		this.setTabConArmazenada(this.getTabConArmazenada() + 1);
		if (this.getTabConArmazenada() > 5) {

			this.setTabConArmazenada(1);

		}
		armazenados = this.getCpfcnpjArmazenado();
		try {

			posix = this.getTabConArmazenada() - 1;
			if (posix < 0) {

				posix = 0;

			}

		} catch (Exception e) {

			posix = 0;

		}

		if (tipopesquisa == 1) {

			armazenados[posix] = mx.getPessoaSite().getEndereco().getLogradouro();

		}

		if (tipopesquisa == 2) {

			armazenados[posix] = mx.getPessoaSite().getCep().getCep();

		}

		if (tipopesquisa == 3) {

			armazenados[posix] = mx.getPessoaSite().getNome();

		}

		try {
			limpaPesquisaAnterior(mx);

			/* codigo padrão para todas as consultas com a paginação */
			if (Integer.parseInt(qtdpesq) < 1) {

				qtdpesq = "1";

			}
			if (Integer.parseInt(qtdpesq) > 100) {

				qtdpesq = "100";
			}
			/* Avança registro */

			/*
			 * Se o comando 0 for passado, sempre vai iniciar a pesquisa do inicio
			 */

			if (comando == 1) {

				pagina = pagina + 1;

			}

			/* Volta registro */
			if (comando == 2) {

				pagina = pagina - 1;
			}

			paginaFinal = Integer.parseInt(qtdpesq) * pagina;
			paginaInicial = (paginaFinal - Integer.parseInt(qtdpesq)) + 1;

			if (pagina == 1 || pagina < 1) {

				paginaFinal = Integer.parseInt(qtdpesq);
				paginaInicial = 1;
				pagina = 1;
			}

			/* 250 e hardcode e o maximo de registros que pesquisamos */
			if (pagina > (250 / Integer.parseInt(qtdpesq))) {

				pagina = 250 / Integer.parseInt(qtdpesq);
				paginaFinal = 250;
				paginaInicial = (paginaFinal - Integer.parseInt(qtdpesq)) + 1;
			}

			if (comando == 0) {

				pagina = 1;
				paginaFinal = Integer.parseInt(qtdpesq);
				paginaInicial = 1;

			}
			mx.setPaginaVizinho(pagina);
			paginaFinal += 1;

			sql.append("SELECT * FROM ( ");
			sql.append(" SELECT PAGINA.*,ROWNUM PAGINA_RN FROM ( ");

			/* pesquisa por endereço */

			if (tipopesquisa == 1) {

				mx.setPaginaEndereco(pagina);
				mx.setResposta_endereco(true);
				mx.setResposta_cep(false);
				mx.setResposta_nome(false);
				mx.setResposta_razao(false);
				mx.setResposta_veiculo(false);
				mx.setResposta_razao(false);
				mx.setResposta_historico_credito(false);
				mx.setResposta_consulta(false);
				mx.setResposta_conArmazenada(false);
				mx.setResposta_operadora(false);
				mx.setResposta_obitoNacional(false);

				if (!estaVazioOuNulo(mx.getPessoaSite().getEndereco().getLogradouro())) {

					// logradouro =
					// this.RetiraTipoLog(mx.getPessoaSite().getEndereco().getLogradouro());
					// logradouro = logradouro.replace(" ","%");

					sql1.append(
							" SELECT CEP FROM MEGA_CEP M WHERE M.LOGRADOURO LIKE REPLACE(REMOVE_TIPO_LOGRADOURO( ? ),' ','%') ");
					resultado2.addString(mx.getPessoaSite().getEndereco().getLogradouro());

					if (!estaVazioOuNulo(mx.getPessoaSite().getEndereco().getCidade())) {
						sql1.append(" AND M.CIDADE= ? ");
						resultado2.addString(mx.getPessoaSite().getEndereco().getCidade());
					}
					if (!estaVazioOuNulo(mx.getPessoaSite().getEndereco().getBairro())) {
						sql1.append(" AND M.BAIRRO= ? ");
						resultado2.addString(mx.getPessoaSite().getEndereco().getBairro());
					}
					if (!estaVazioOuNulo(mx.getPessoaSite().getEndereco().getUf().getSigla())) {
						sql1.append(" AND M.UF= ? ");
						resultado2.addString(mx.getPessoaSite().getEndereco().getUf().getSigla());
					}

					resultado2.setSql(sql1.toString());
				}

				cepPesq = this.pesquisaCep(resultado2, mx);
				sql.append(" SELECT /*+INDEX (T IDXCEPN)*/ ");
				sql.append(
						" (SELECT STATUS FROM TELEFONES_STATUS WHERE TELEFONE = T.TELEFONE AND ROWNUM <= 1) AS STATUS_LINHA,");
				sql.append(
						" (CASE WHEN (SELECT FLAG FROM TBL_WHATSAPP T2 WHERE T2.TELEFONE = T.TELEFONE AND ROWNUM <= 1) = 1 THEN 1 ELSE 0 END) AS WHATSAPP, ");
				sql.append(
						" (SELECT IS_DATE_CONFIRME(DT_OBITO) FROM OBITO WHERE NU_CPF = CPFCGC AND ROWNUM<=1 ) AS OBITO,");
				sql.append(
						" (SELECT DT_ABERTURA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS DT_ABERTURA,");
				sql.append(" (SELECT SITUACAO FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS SITUACAO,");
				sql.append(" (SELECT FANTASIA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS FANTASIA,");
				sql.append(
						" (SELECT DESC_NATUREZA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS NATUREZA,");
				sql.append(
						" I.NOME,T.PROPRIETARIO AS PROPRIETARIO,T.ATUAL,T.TELEFONE,T.ENDERECO AS ENDERECO,T.NUMERO AS NUMERO,T.COMPLEMENTO,T.BAIRRO,T.CEP,T.CIDADE,T.UF,T.CPFCGC AS CPFCNPJ,");
				sql.append(
						" i.CPF_CONJUGE AS CPF_CONJUGE,PEGA_NOME_INFO_COMP(I.CPF_CONJUGE) AS NOME_CONJUGE,I.NOME_MAE AS MAE,I.NOME_PAI AS PAI,TO_CHAR(I.DATA_NASC,'DD/MM/YYYY') AS NASC,' ' AS TITULO,T.OPERADORA,TO_CHAR(T.DT_INSTALACAO,'YYYY-MM-DD') AS DT_INSTALACAO, I.SIGNO AS SIGNO");
				sql.append(
						" FROM TELEFONES T, MEGA_CEP M, INFO_COMPLEMENTARES I WHERE T.CEP=M.CEP AND T.CPFCGC=I.CPFCNPJ(+) AND "
			/*						+ cepPesq.getCompletaSql() + "");
				resultado.addString(cepPesq.getBind());*/
						
						+ cepPesq.getSql() + "");
				
				for (int i = 0; i < cepPesq.getBinds().size(); i++) {
					 resultado.addString(cepPesq.getBinds().get(i));
					
				}
				

				// Se o bairro foi colocado, poe no SQL para ficar mais rápido
				if (!estaVazioOuNulo(mx.getPessoaSite().getEndereco().getNumeroinicial())
						&& (!estaVazioOuNulo(mx.getPessoaSite().getEndereco().getNumerofinal()))) {

					sql.append(" AND T.NUMERO >= ? AND T.NUMERO <= ? ");
					resultado.addString(mx.getPessoaSite().getEndereco().getNumeroinicial());
					resultado.addString(mx.getPessoaSite().getEndereco().getNumerofinal());

				}

				if (!(estaVazioOuNulo(mx.getPessoaSite().getEndereco().getNumeroinicial()))
						&& (estaVazioOuNulo(mx.getPessoaSite().getEndereco().getNumerofinal()))) {

					sql.append(" AND T.NUMERO >= ? ");
					resultado.addString(mx.getPessoaSite().getEndereco().getNumeroinicial());

				}
				if (estaVazioOuNulo(mx.getPessoaSite().getEndereco().getNumeroinicial())
						&& !estaVazioOuNulo(mx.getPessoaSite().getEndereco().getNumerofinal())) {

					sql.append(" AND T.NUMERO <= ? ");
					resultado.addString(mx.getPessoaSite().getEndereco().getNumerofinal());

				}

				if (!estaVazioOuNulo(mx.getPessoaSite().getEndereco().getComplemento())) {

					sql.append("AND T.COMPLEMENTO LIKE  ? % ");
					resultado.addString(mx.getPessoaSite().getEndereco().getComplemento());

				}

				if (!estaVazioOuNulo(mx.getPessoaSite().getEndereco().getNome())) {

					sql.append("AND T.PROPRIETARIO LIKE ? %");
					resultado.addString(mx.getPessoaSite().getEndereco().getNome());
				}

				// Fechamento do SQL
				sql.append("AND NOT EXISTS (SELECT * FROM PROTECAO_CPFCNPJ WHERE USUARIO = ? AND CPFCGC = T.CPFCGC ) ");
				resultado.addString(mx.getUsuario().getLogin());
				// sql = sql + " ORDER BY ATUAL DESC";

				String[] nomeservidor = mx.getServidor();
				Conexao.registraConsulta(conn, "CO-ENDERECO", mx.getPessoaSite().getCpfcnpj(),
						mx.getUsuario().getLogin(), mx.getUsuario().getSenha(), "CONFI", mx.getUsuario().getIP(),
						mx.getCanonicalName());
				resultado.setSql(sql.toString());
				resultado = montaFinalDaConsulta(resultado, paginaInicial, paginaFinal);
				sql = new StringBuilder(resultado.getSql());
			}

			/* pesquisa por cep */

			if (tipopesquisa == 2) {

				mx.setPaginaEndereco(pagina);
				mx.setResposta_endereco(false);
				mx.setResposta_cep(true);
				mx.setResposta_nome(false);
				mx.setResposta_razao(false);
				mx.setResposta_veiculo(false);
				mx.setResposta_historico_credito(false);
				mx.setResposta_consulta(false);
				mx.setResposta_conArmazenada(false);
				mx.setResposta_operadora(false);
				mx.setResposta_obitoNacional(false);

				sql.append(" SELECT ");
				sql.append(
						" (SELECT STATUS FROM TELEFONES_STATUS WHERE TELEFONE = T.TELEFONE AND ROWNUM <= 1) AS STATUS_LINHA,");
				sql.append(
						" (CASE WHEN (SELECT FLAG FROM TBL_WHATSAPP T2 WHERE T2.TELEFONE = T.TELEFONE AND ROWNUM <= 1) = 1 THEN 1 ELSE 0 END) AS WHATSAPP, ");
				sql.append(
						" (SELECT IS_DATE_CONFIRME(DT_OBITO) FROM OBITO WHERE NU_CPF = CPFCGC AND ROWNUM<=1 ) AS OBITO,");
				sql.append(
						" (SELECT DT_ABERTURA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS DT_ABERTURA,");
				sql.append(" (SELECT SITUACAO FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS SITUACAO,");
				sql.append(" (SELECT FANTASIA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS FANTASIA,");
				sql.append(
						" (SELECT DESC_NATUREZA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS NATUREZA,");
				sql.append(
						" I.NOME,T.PROPRIETARIO AS PROPRIETARIO,T.ATUAL,T.TELEFONE,T.ENDERECO AS ENDERECO,T.NUMERO AS NUMERO,T.COMPLEMENTO,T.BAIRRO,T.CEP,T.CIDADE,T.UF,T.CPFCGC AS CPFCNPJ,");
				sql.append(
						" i.CPF_CONJUGE AS CPF_CONJUGE,PEGA_NOME_INFO_COMP(I.CPF_CONJUGE) AS NOME_CONJUGE,I.NOME_MAE AS MAE,I.NOME_PAI AS PAI,TO_CHAR(I.DATA_NASC,'DD/MM/YYYY') AS NASC,' ' AS TITULO,T.OPERADORA,TO_CHAR(T.DT_INSTALACAO,'YYYY-MM-DD') AS DT_INSTALACAO, I.SIGNO AS SIGNO");
				sql.append(" FROM TELEFONES T, MEGA_CEP M, INFO_COMPLEMENTARES I WHERE T.CEP=M.CEP(+) ");
				sql.append(" AND T.CPFCGC=I.CPFCNPJ(+) AND T.CEP = ? ");

				resultado.addString(mx.getPessoaSite().getCep().getCep());

				// Se o bairro foi colocado, poe no SQL para ficar mais rápido

				// Operadoras Selecionadas
				if (!operadoras.isEmpty()) {
					sql.append(" AND T.OPERADORA IN (?)");
					resultado.addString(retornaOperadorasSelecionadas());
				}

				if (!estaVazioOuNulo(mx.getPessoaSite().getCep().getNumeroinicial())
						&& !estaVazioOuNulo(mx.getPessoaSite().getCep().getNumerofinal())) {

					sql.append(" AND T.NUMERO >= ? AND T.NUMERO <= ? ");
					resultado.addString(mx.getPessoaSite().getCep().getNumeroinicial());
					resultado.addString(mx.getPessoaSite().getCep().getNumerofinal());

				}

				if (!estaVazioOuNulo(mx.getPessoaSite().getCep().getNumeroinicial())
						&& estaVazioOuNulo(mx.getPessoaSite().getCep().getNumerofinal())) {

					sql.append(" AND T.NUMERO >= ? ");
					resultado.addString(mx.getPessoaSite().getCep().getNumeroinicial());

				}
				if (estaVazioOuNulo(mx.getPessoaSite().getCep().getNumeroinicial())
						&& !estaVazioOuNulo(mx.getPessoaSite().getCep().getNumerofinal())) {

					sql.append(" AND T.NUMERO <= ? ");
					resultado.addString(mx.getPessoaSite().getCep().getNumerofinal());

				}

				if (!estaVazioOuNulo(mx.getPessoaSite().getCep().getComplemento())) {

					sql.append("AND T.COMPLEMENTO LIKE %?% ");
					resultado.addString(mx.getPessoaSite().getCep().getComplemento());

				}

				if (!estaVazioOuNulo(mx.getPessoaSite().getCep().getNome())) {

					sql.append("AND T.PROPRIETARIO LIKE ?% ");
					resultado.addString(mx.getPessoaSite().getCep().getNome());

				}

				// Fechamento do SQL
				sql.append("AND NOT EXISTS (SELECT * FROM PROTECAO_CPFCNPJ WHERE USUARIO = ? AND CPFCGC = T.CPFCGC ) ");
				resultado.addString(mx.getUsuario().getLogin());
				resultado.setSql(sql.toString());
				// sql = sql + " ORDER BY ATUAL DESC";

				String[] nomeservidor = mx.getServidor();
				Conexao.registraConsulta(conn, "CO-CEP", mx.getPessoaSite().getCpfcnpj(), mx.getUsuario().getLogin(),
						mx.getUsuario().getSenha(), "CONFI", mx.getUsuario().getIP(), mx.getCanonicalName());
				resultado = montaFinalDaConsulta(resultado, paginaInicial, paginaFinal);
				// sql = new StringBuilder(resultado.getSql());

			}

			/* pesquisa por nome */

			if (tipopesquisa == 3) {

				mx.setPaginaEndereco(pagina);
				mx.setResposta_endereco(false);
				mx.setResposta_cep(false);
				mx.setResposta_nome(true);
				mx.setResposta_razao(false);
				mx.setResposta_veiculo(false);
				mx.setResposta_historico_credito(false);
				mx.setResposta_consulta(false);
				mx.setResposta_conArmazenada(false);
				mx.setResposta_operadora(false);
				mx.setResposta_obitoNacional(false);

				mx.setPaginaNome(pagina);

				String[] nomeservidor = mx.getServidor();
				Conexao.registraConsulta(conn, "NOME", mx.getPessoaSite().getCpfcnpj(), mx.getUsuario().getLogin(),
						mx.getUsuario().getSenha(), "CONFI", mx.getUsuario().getIP(), mx.getCanonicalName());

			}

			if (tipopesquisa == 6) {
				mx.setPaginaEndereco(pagina);
				mx.setResposta_endereco(false);
				mx.setResposta_cep(false);
				mx.setResposta_nome(false);
				mx.setResposta_razao(true);
				mx.setResposta_veiculo(false);
				mx.setResposta_historico_credito(false);
				mx.setResposta_consulta(false);
				mx.setResposta_conArmazenada(false);
				mx.setResposta_operadora(false);
				mx.setResposta_obitoNacional(false);

				mx.setPaginaNome(pagina);

				String[] nomeservidor = mx.getServidor();
				Conexao.registraConsulta(conn, "RAZAO_SOCIAL", mx.getPessoaSite().getCpfcnpj(),
						mx.getUsuario().getLogin(), mx.getUsuario().getSenha(), "CONFI", mx.getUsuario().getIP(),
						mx.getCanonicalName());
			}

			mx.setResposta_consulta(false);
			mx.setResposta_conArmazenada(false);

			ok = processaConsultaVizinhos(resultado, tipopesquisa, mx, paginaInicial, paginaFinal);

		} catch (Exception e) {
			ok = false;
		} finally {
			this.possuiConexao = false;
			releaseConnection();
		}
		return ok;
	}

	public Boolean pesquisaFilhos(LoginMBean mb, int comando) {
		/*
		 * Método de pesquisa Multimpla esta pesquisa possui sempre o mesmo tipo de
		 * retorno a mudança esta apenas na query
		 *
		 * Este método usa a classe de vizinhos para mostrar os resultados By SMarcio em
		 * 12/11/2013
		 */

		java.sql.Statement stmtN;
		ResultSet rs;
		StringBuilder sql = new StringBuilder();
		Boolean achou = false;

		this.filhos = new ArrayList<Parentes>();

		if (this.infocomplementares.getCpfcnpj().length() == 11) {
			try {
				/* codigo padrão para todas as consultas com a paginação */
				List<Integer> paginas = getPaginas(mb.getPaginaFilhos(), comando, mb);
				Integer paginaInicial = paginas.get(0);
				Integer paginaFinal = paginas.get(1);
				mb.setPaginaFilhos(paginas.get(2));
				SqlToBind resultado = new SqlToBind();

				sql.append("SELECT * FROM ( ");
				sql.append(" SELECT PAGINA.*,ROWNUM PAGINA_RN FROM ( ");

				resultado.setSql(sql.toString());
				resultado = montaConsultaFilhosFromInfoComplementares(mb, resultado);

				resultado = montaFinalDaConsulta(resultado, paginaInicial, paginaFinal);
				sql = new StringBuilder(resultado.getSql());

				String[] nomeservidor = mb.getServidor();
				// mb.getObjconexao().registraConsulta(conn, "FILHOS",
				// mb.getPessoaSite().getCpfcnpj(), mb.getUsuario().getLogin(),
				// mb.getUsuario().getSenha(),
				// "CONFI",mb.getUsuario().getIP(),nomeservidor[2]);

				achou = processaConsultaFilhos(resultado, 1, mb, mb.getQtdpesq());
				mb.setM_filhos(achou);

				String mensagem = "";
				if (!achou && this.exibirMensagem) {
					mensagem = "Não constam Filhos.";
				}
				FacesContext facesContext = FacesContext.getCurrentInstance();
				FacesMessage msg = new FacesMessage(mensagem);
				facesContext.addMessage("formResposta:searchButtonsError", msg);
			} catch (Exception e) {
				logger.error("Erro no metodo pesquisaFilhos da classe Resposta: " + e.getMessage());
				achou = false;
			}
		}
		return achou;

	}

	public Boolean pesquisaVizinhos(LoginMBean mb, int comando, Boolean vizinhoCentro, Boolean vizinhoEsquerda,
			Boolean vizinhoDireita) {
		/* processamento de vizinhos */
		StringBuilder sql = new StringBuilder();
		SqlToBind resultado = new SqlToBind();
		Boolean achou = false;
		this.vizinhos = new ArrayList<Vizinhos>();
		if (vizinhoCentro || vizinhoDireita || vizinhoEsquerda) {
			try {
				this.lastVizinhoCentro = vizinhoCentro;
				this.lastVizinhoDireita = vizinhoDireita;
				this.lastVizinhoEsquerda = vizinhoEsquerda;

				List<Integer> paginas = getPaginas(mb.getPaginaVizinho(), comando, mb);
				Integer paginaInicial = paginas.get(0);
				Integer paginaFinal = paginas.get(1);
				mb.setPaginaVizinho(paginas.get(2));

				if (this.getTelefone().get(0).getCep() != null) {
					sql.delete(0, sql.length());
					sql.append(" SELECT * FROM ( ");
					sql.append(" SELECT PAGINA.*,ROWNUM PAGINA_RN FROM ( ");
					sql.append(" SELECT * FROM ( ");

					if (vizinhoCentro == true) {

						sql.append(
								" SELECT (SELECT STATUS FROM TELEFONES_STATUS WHERE TELEFONE = T.TELEFONE AND ROWNUM <= 1) AS STATUS_LINHA,");
						sql.append(
								" (CASE WHEN (SELECT FLAG FROM TBL_WHATSAPP T2 WHERE T2.TELEFONE = T.TELEFONE AND ROWNUM <= 1) = 1 THEN 1 ELSE 0 END) AS WHATSAPP, ");
						sql.append(
								" (SELECT IS_DATE_CONFIRME(DT_OBITO) FROM OBITO WHERE NU_CPF = CPFCGC AND ROWNUM<=1 ) AS OBITO,");
						sql.append(
								" I.NOME,T.PROPRIETARIO AS PROPRIETARIO,T.ATUAL,T.TELEFONE,T.ENDERECO AS ENDERECO,T.NUMERO AS NUMERO,T.COMPLEMENTO,T.BAIRRO,T.CEP,T.CIDADE,T.UF,T.CPFCGC AS CPFCNPJ,");
						sql.append(
								" i.CPF_CONJUGE AS CPF_CONJUGE,PEGA_NOME_INFO_COMP(I.CPF_CONJUGE) AS NOME_CONJUGE,I.NOME_MAE AS MAE,I.NOME_PAI AS PAI,TO_CHAR(I.DATA_NASC,'DD/MM/YYYY') AS NASC,' ' AS TITULO,T.OPERADORA,TO_CHAR(T.DT_INSTALACAO,'YYYY-MM-DD') AS DT_INSTALACAO,  I.SIGNO AS SIGNO ");
						sql.append(
								" FROM TELEFONES T, INFO_COMPLEMENTARES I WHERE T.PROPRIETARIO IS NOT NULL AND T.CPFCGC = I.CPFCNPJ(+) ");
						sql.append(" AND T.CEP= ? AND T.NUMERO= ? AND T.CPFCGC<> ? AND ROWNUM <= ? ");

						resultado.addString(this.getTelefone().get(0).getCep());
						resultado.addString(this.getTelefone().get(0).getNumero());
						resultado.addString(mb.getPessoaSite().getCpfcnpj());
						resultado.addString(String.valueOf(br.com.confirmeonline.util.SQLConstantes.QTD_MAX_PESQUISA));

						if (vizinhoDireita || vizinhoEsquerda) {

							sql.append("UNION ALL ");

						}

					}
					if (vizinhoDireita == true) {
						sql.append(
								"SELECT (SELECT STATUS FROM TELEFONES_STATUS WHERE TELEFONE = T.TELEFONE AND ROWNUM <= 1) AS STATUS_LINHA,");
						sql.append(
								" (CASE WHEN (SELECT FLAG FROM TBL_WHATSAPP T2 WHERE T2.TELEFONE = T.TELEFONE AND ROWNUM <= 1) = 1 THEN 1 ELSE 0 END) AS WHATSAPP, ");
						sql.append(
								"(SELECT IS_DATE_CONFIRME(DT_OBITO) FROM OBITO WHERE NU_CPF = CPFCGC AND ROWNUM<=1 ) AS OBITO,");
						sql.append(
								"I.NOME,T.PROPRIETARIO AS PROPRIETARIO,T.ATUAL,T.TELEFONE,T.ENDERECO AS ENDERECO,T.NUMERO AS NUMERO,T.COMPLEMENTO,T.BAIRRO,T.CEP,T.CIDADE,T.UF,T.CPFCGC AS CPFCNPJ,");
						sql.append(
								"i.CPF_CONJUGE AS CPF_CONJUGE,PEGA_NOME_INFO_COMP(I.CPF_CONJUGE) AS NOME_CONJUGE,I.NOME_MAE AS MAE,I.NOME_PAI AS PAI,TO_CHAR(I.DATA_NASC,'DD/MM/YYYY') AS NASC,' ' AS TITULO,T.OPERADORA,TO_CHAR(T.DT_INSTALACAO,'YYYY-MM-DD') AS DT_INSTALACAO, I.SIGNO AS SIGNO ");
						sql.append(
								"FROM TELEFONES T, INFO_COMPLEMENTARES I WHERE T.PROPRIETARIO IS NOT NULL AND T.CPFCGC = I.CPFCNPJ(+) ");
						sql.append("AND T.CEP= ? AND T.NUMERO >=( ? - 30) AND T.NUMERO < ? AND ROWNUM <= ? ");

						resultado.addString(this.getTelefone().get(0).getCep());
						resultado.addString(this.getTelefone().get(0).getNumero());
						resultado.addString(this.getTelefone().get(0).getNumero());
						resultado.addString(String.valueOf(br.com.confirmeonline.util.SQLConstantes.QTD_MAX_PESQUISA));

						if (vizinhoEsquerda == true) {

							sql.append("UNION ALL ");

						}
					}

					if (vizinhoEsquerda == true) {

						sql.append(
								"SELECT (SELECT STATUS FROM TELEFONES_STATUS WHERE TELEFONE = T.TELEFONE AND ROWNUM <= 1) AS STATUS_LINHA,");
						sql.append(
								" (CASE WHEN (SELECT FLAG FROM TBL_WHATSAPP T2 WHERE T2.TELEFONE = T.TELEFONE AND ROWNUM <= 1) = 1 THEN 1 ELSE 0 END) AS WHATSAPP, ");
						sql.append(
								"(SELECT IS_DATE_CONFIRME(DT_OBITO) FROM OBITO WHERE NU_CPF = CPFCGC AND ROWNUM<=1 ) AS OBITO,");
						sql.append(
								"I.NOME,T.PROPRIETARIO AS PROPRIETARIO,T.ATUAL,T.TELEFONE,T.ENDERECO AS ENDERECO,T.NUMERO AS NUMERO,T.COMPLEMENTO,T.BAIRRO,T.CEP,T.CIDADE,T.UF,T.CPFCGC AS CPFCNPJ,");
						sql.append(
								"i.CPF_CONJUGE  AS CPF_CONJUGE,PEGA_NOME_INFO_COMP(I.CPF_CONJUGE) AS NOME_CONJUGE,I.NOME_MAE AS MAE,I.NOME_PAI AS PAI,TO_CHAR(I.DATA_NASC,'DD/MM/YYYY') AS NASC,' ' AS TITULO,T.OPERADORA,TO_CHAR(T.DT_INSTALACAO,'YYYY-MM-DD') AS DT_INSTALACAO, I.SIGNO AS SIGNO ");
						sql.append(
								"FROM TELEFONES T, INFO_COMPLEMENTARES I WHERE T.PROPRIETARIO IS NOT NULL AND T.CPFCGC = I.CPFCNPJ(+) ");
						sql.append("AND T.CEP= ? AND T.NUMERO <=(? + 30) AND T.NUMERO > ? "
								+ "AND T.CPFCGC<> ? AND ROWNUM <= ? ");

						resultado.addString(this.getTelefone().get(0).getCep());
						resultado.addString(this.getTelefone().get(0).getNumero());
						resultado.addString(this.getTelefone().get(0).getNumero());
						resultado.addString(mb.getPessoaSite().getCpfcnpj());
						resultado.addString(String.valueOf(br.com.confirmeonline.util.SQLConstantes.QTD_MAX_PESQUISA));
					}
					sql.append(" ) WHERE ROWNUM <= " + br.com.confirmeonline.util.SQLConstantes.QTD_MAX_PESQUISA
							+ " ORDER BY CPFCNPJ, TO_NUMBER(ATUAL) DESC, WHATSAPP ) PAGINA  ) WHERE ( PAGINA_RN >= ? AND PAGINA_RN <= ? ) ");

					resultado.addString(String.valueOf(paginaInicial));
					resultado.addString(String.valueOf(paginaFinal));

					// sql.append(" ) ORDER BY NUMERO, COMPLEMENTO, PROPRIETARIO
					// ) PAGINA WHERE ( ROWNUM <=
					// "+Util.SQLConstantes.QTD_MAX_PESQUISA+" ) ) WHERE (
					// PAGINA_RN >= '"+ paginaInicial + "' AND PAGINA_RN <= '"+
					// paginaFinal + "' ) ");
					// sql.append(" ORDER BY CPFCNPJ,NUMERO, COMPLEMENTO,
					// PROPRIETARIO");
					resultado.setSql(sql.toString());
					mb.setM_vizinho(true);
					achou = processaConsultaVizinhos(resultado, 1, mb, paginaInicial, paginaFinal);
					if (achou == false) {

						mb.setM_vizinho(false);

					}
					// String [] nomeservidor = mb.getServidor();
					// mb.getObjconexao().registraConsulta(conn, "VIZINHO",
					// mb.getPessoaSite().getTelefone(),
					// mb.getUsuario().getLogin(), mb.getUsuario().getSenha(),
					// "CONFI",mb.getUsuario().getIP(),nomeservidor[2]);

				}
			} catch (Exception e) {
				logger.error("Errro no metodo pesquisa_vizinhos da classe Resposta: " + e.getMessage());
				/* o cara nao tem telefone e nao tem vizinhos */
				achou = false;
				mb.setM_vizinho(false);

			}
			String mensagem = "";
			if (!achou && this.exibirMensagem) {
				mensagem = "Não constam Vizinhos.";
			}
			FacesContext facesContext = FacesContext.getCurrentInstance();
			FacesMessage msg = new FacesMessage(mensagem);
			facesContext.addMessage("formResposta:searchButtonsError", msg);
		}

		return achou;
	}

	public Boolean pesquisaParentes(LoginMBean mb, int comando) {
		SqlToBind sql = new SqlToBind();
		Boolean achou = false;
		Boolean okp = false;

		if (bPesquisaDM_Parente == true) {
			okp = pesquisaParentesDM_Parentes(mb, comando);
		} else {

			try {

				this.parentes = new ArrayList<Parentes>();

				List<Integer> paginas = getPaginas(mb.getPaginaParentes(), comando, mb);
				Integer paginaInicial = paginas.get(0);
				Integer paginaFinal = paginas.get(1);
				mb.setPaginaParentes(paginas.get(2));
				String nomeMae = this.getInfocomplementares().getNomemae();
				String cpf = this.getInfocomplementares().getCpfcnpj();

				if (!estaVazioOuNulo(cpf) && !estaVazioOuNulo(nomeMae)) {

					sql = processaPesquisaParentes(nomeMae, cpf, cpf.substring(8, 9),
							br.com.confirmeonline.util.SQLConstantes.QTD_MAX_PESQUISA, paginaInicial, paginaFinal);

					// sql.append(" ORDER BY CPFCNPJ,NUMERO, COMPLEMENTO,
					// PROPRIETARIO");
					// sql.append(" ) ORDER BY NUMERO, COMPLEMENTO, PROPRIETARIO
					// ) PAGINA WHERE ( ROWNUM <=
					// "+Util.SQLConstantes.QTD_MAX_PESQUISA+" ) ) WHERE (
					// PAGINA_RN >= '"+ paginaInicial + "' AND PAGINA_RN <= '"+
					// paginaFinal + "' ) ");
					// sql.append(" ORDER BY CPFCNPJ,NUMERO, COMPLEMENTO,
					// PROPRIETARIO");

					achou = processaConsultaParentes(sql, 1, mb);
				}
				mb.setM_parentes(achou);

				String[] nomeservidor = mb.getServidor();
				// mb.getObjconexao().registraConsulta(conn, "PARENTE",
				// mb.getPessoaSite().getTelefone(), mb.getUsuario().getLogin(),
				// mb.getUsuario().getSenha(),
				// "CONFI",mb.getUsuario().getIP(),nomeservidor[2]);
			} catch (Exception e) {
				achou = false;
				mb.setF_moradores(false);
				mb.setM_parentes(false);
			}
			String mensagem = "";
			if (!achou && this.exibirMensagem) {
				mensagem = "Não constam Parentes.";
			}
			FacesContext facesContext = FacesContext.getCurrentInstance();
			FacesMessage msg = new FacesMessage(mensagem);
			facesContext.addMessage("formResposta:searchButtonsError", msg);

		}
		return achou;
	}

	private SqlToBind processaPesquisaParentes(String nomeMae, String cpf, String substring, int qtdMaxPesquisa,
			Integer paginaInicial, Integer paginaFinal) {

		StringBuilder sql = new StringBuilder();
		SqlToBind resultado = new SqlToBind();

		sql.delete(0, sql.length());
		sql.append(" SELECT * FROM ( ");
		sql.append(" SELECT PAGINA.*,ROWNUM PAGINA_RN FROM ( ");
		sql.append(" SELECT * FROM ( ");
		sql.append(
				" SELECT (SELECT STATUS FROM TELEFONES_STATUS WHERE TELEFONE = T.TELEFONE AND ROWNUM <= 1) AS STATUS_LINHA,");
		sql.append(
				" (CASE WHEN (SELECT FLAG FROM TBL_WHATSAPP T2 WHERE T2.TELEFONE = T.TELEFONE AND ROWNUM <= 1) = 1 THEN 1 ELSE 0 END) AS WHATSAPP, ");
		sql.append(" (SELECT IS_DATE_CONFIRME(DT_OBITO) FROM OBITO WHERE NU_CPF = CPFCGC AND ROWNUM<=1 ) AS OBITO,");
		sql.append(
				" I.NOME,T.PROPRIETARIO AS PROPRIETARIO,T.ATUAL,T.TELEFONE,T.ENDERECO AS ENDERECO,T.NUMERO AS NUMERO,T.COMPLEMENTO,T.BAIRRO,T.CEP,T.CIDADE,T.UF,I.CPFCNPJ AS CPFCNPJ,");
		sql.append(
				" i.CPF_CONJUGE  AS CPF_CONJUGE,PEGA_NOME_INFO_COMP(I.CPF_CONJUGE) AS NOME_CONJUGE,I.NOME_MAE AS MAE,TO_CHAR(I.DATA_NASC,'DD/MM/YYYY') AS NASC,' ' AS TITULO,T.OPERADORA,TO_CHAR(T.DT_INSTALACAO,'YYYY-MM-DD') AS DT_INSTALACAO, I.SIGNO AS SIGNO, null as GRPA_DS_PARENTESCO, null as GRPA_DS_FORMA  ");
		sql.append(" FROM TELEFONES T,INFO_COMPLEMENTARES I,FINAN.CRED_MEGA_CEP M ");
		sql.append(
				" WHERE  I.NOME = ? AND I.CPFCNPJ <> ? AND SUBSTR(I.CPFCNPJ,9,1)= ? AND I.CPFCNPJ = T.CPFCGC(+) AND T.CEP = M.CEP(+) AND ROWNUM <= ? ");
		sql.append(" UNION ALL ");
		sql.append(
				" SELECT (SELECT STATUS FROM TELEFONES_STATUS WHERE TELEFONE = T.TELEFONE AND ROWNUM <= 1) AS STATUS_LINHA,");
		sql.append(
				" (CASE WHEN (SELECT FLAG FROM TBL_WHATSAPP T2 WHERE T2.TELEFONE = T.TELEFONE AND ROWNUM <= 1) = 1 THEN 1 ELSE 0 END) AS WHATSAPP, ");
		sql.append(" (SELECT IS_DATE_CONFIRME(DT_OBITO) FROM OBITO WHERE NU_CPF = CPFCGC AND ROWNUM<=1 ) AS OBITO,");
		sql.append(
				" I.NOME,T.PROPRIETARIO AS PROPRIETARIO,T.ATUAL,T.TELEFONE,T.ENDERECO AS ENDERECO,T.NUMERO AS NUMERO,T.COMPLEMENTO,T.BAIRRO,T.CEP,T.CIDADE,T.UF,I.CPFCNPJ AS CPFCNPJ,");
		sql.append(
				" i.CPF_CONJUGE AS CPF_CONJUGE,PEGA_NOME_INFO_COMP(I.CPF_CONJUGE) AS NOME_CONJUGE,I.NOME_MAE AS MAE,TO_CHAR(I.DATA_NASC,'DD/MM/YYYY') AS NASC,' ' AS TITULO,T.OPERADORA,TO_CHAR(T.DT_INSTALACAO,'YYYY-MM-DD') AS DT_INSTALACAO,I.SIGNO AS SIGNO, null as GRPA_DS_PARENTESCO, null as GRPA_DS_FORMA   ");
		sql.append(" FROM TELEFONES T,INFO_COMPLEMENTARES I,FINAN.CRED_MEGA_CEP M ");
		sql.append(
				" WHERE I.NOME_MAE = ? AND I.CPFCNPJ <> ? AND SUBSTR(I.CPFCNPJ,9,1)= ? AND I.CPFCNPJ = T.CPFCGC(+) AND T.CEP = M.CEP(+) AND ROWNUM <= ?");
		sql.append(
				" ) WHERE ROWNUM <= ? ORDER BY CPFCNPJ, TO_NUMBER(ATUAL) DESC, WHATSAPP DESC ) PAGINA ) WHERE ( PAGINA_RN >= ? AND PAGINA_RN <= ? ) ");

		resultado.limpaLista();
		resultado.addString(nomeMae);
		resultado.addString(cpf);
		resultado.addString(substring);
		resultado.addString(String.valueOf(qtdMaxPesquisa));
		resultado.addString(nomeMae);
		resultado.addString(cpf);
		resultado.addString(substring);
		resultado.addString(String.valueOf(qtdMaxPesquisa));
		resultado.addString(String.valueOf(qtdMaxPesquisa));
		resultado.addString(String.valueOf(paginaInicial));
		resultado.addString(String.valueOf(paginaFinal));

		resultado.setSql(sql.toString());
		return resultado;
	}

	public Boolean pesquisaParentesDM_Parentes(LoginMBean mb, int comando) {
		SqlToBind sql = new SqlToBind();
		Boolean achou = false;

		try {

			this.parentes = new ArrayList<Parentes>();

			List<Integer> paginas = getPaginas(mb.getPaginaParentes(), comando, mb);
			Integer paginaInicial = paginas.get(0);
			Integer paginaFinal = paginas.get(1);
			mb.setPaginaParentes(paginas.get(2));
			String nomeMae = this.getInfocomplementares().getNomemae();
			String cpf = this.getInfocomplementares().getCpfcnpj();

			if (!estaVazioOuNulo(cpf)) {

				sql = montapesquisaParentesDM_Parentes(cpf, br.com.confirmeonline.util.SQLConstantes.QTD_MAX_PESQUISA,
						paginaInicial, paginaFinal);

				achou = processaConsultaParentes(sql, 1, mb);

				if (achou == true) {
					bPesquisaDM_Parente = true;
				} else {
					bPesquisaDM_Parente = false;
				}

			}
			mb.setM_parentes(achou);

			String[] nomeservidor = mb.getServidor();
			// mb.getObjconexao().registraConsulta(conn, "PARENTE",
			// mb.getPessoaSite().getTelefone(), mb.getUsuario().getLogin(),
			// mb.getUsuario().getSenha(),
			// "CONFI",mb.getUsuario().getIP(),nomeservidor[2]);
		} catch (Exception e) {
			achou = false;
			mb.setF_moradores(false);
			mb.setM_parentes(false);
			logger.error("Erro no metodo pesquisaParentesDM_Parentes da classe Resposta " + e.getMessage());
		}
		String mensagem = "";
		if (!achou && this.exibirMensagem) {
			mensagem = "Não constam Parentes.";
		}
		FacesContext facesContext = FacesContext.getCurrentInstance();
		FacesMessage msg = new FacesMessage(mensagem);
		facesContext.addMessage("formResposta:searchButtonsError", msg);

		return achou;
	}

	private SqlToBind montapesquisaParentesDM_Parentes(String cpf, int qtdMaxPesquisa, Integer paginaInicial,
			Integer paginaFinal) {

		StringBuilder sql = new StringBuilder();
		SqlToBind resultado = new SqlToBind();

		sql.delete(0, sql.length());
		sql.append(" select * from ( ");
		sql.append(" select pagina.*, ROWNUM PAGINA_RN  from ( ");
		sql.append(
				" select (SELECT STATUS  FROM TELEFONES_STATUS WHERE TELEFONE = T.TELEFONE AND ROWNUM <= 1) AS STATUS_LINHA,");
		sql.append(
				" (CASE WHEN (SELECT FLAG FROM TBL_WHATSAPP T2 WHERE T2.TELEFONE = T.TELEFONE AND ROWNUM <= 1) = 1 THEN 1 ELSE 0 END) AS WHATSAPP, ");
		sql.append(" (SELECT IS_DATE_CONFIRME(DT_OBITO) FROM OBITO WHERE NU_CPF = CPFCGC AND ROWNUM<=1 ) AS OBITO,");
		sql.append(
				" I.NOME,T.PROPRIETARIO AS PROPRIETARIO,T.ATUAL,T.TELEFONE,T.ENDERECO AS ENDERECO,T.NUMERO AS NUMERO,T.COMPLEMENTO,T.BAIRRO, ");
		sql.append(" T.CEP,T.CIDADE,T.UF,I.CPFCNPJ AS CPFCNPJ,");
		sql.append(
				" i.CPF_CONJUGE  AS CPF_CONJUGE,PEGA_NOME_INFO_COMP(I.CPF_CONJUGE) AS NOME_CONJUGE,I.NOME_MAE AS MAE, ");
		sql.append(
				" TO_CHAR(I.DATA_NASC,'DD/MM/YYYY') AS NASC,' ' AS TITULO,T.OPERADORA,TO_CHAR(T.DT_INSTALACAO,'YYYY-MM-DD') AS DT_INSTALACAO, ");
		sql.append(" I.SIGNO AS SIGNO, ");
		sql.append(" dom.GRPA_DS_PARENTESCO as GRPA_DS_PARENTESCO, dom.GRPA_DS_FORMA as GRPA_DS_FORMA ");
		sql.append(" FROM dm_parente pa, dm_grau_parentesco dom, telefones t, info_complementares i ");
		sql.append(" WHERE  pa.grpa_id_parentesco = DOM.GRPA_ID_PARENTESCO ");
		sql.append("  and pa.pare_cd_cpf_parente = T.CPFCGC (+) ");
		sql.append("  and pa.pare_cd_cpf_parente = I.CPFCNPJ ");
		sql.append("  and pa.pare_cd_cpf = ? ");
		sql.append("  AND ROWNUM <= ? ");
		sql.append("  ) PAGINA ) WHERE ( PAGINA_RN >= ? AND PAGINA_RN <= ?) ");

		resultado.limpaLista();
		resultado.addString(cpf);
		resultado.addString(String.valueOf(qtdMaxPesquisa));
		resultado.addString(String.valueOf(paginaInicial));
		resultado.addString(String.valueOf(paginaFinal));

		resultado.setSql(sql.toString());

		return resultado;

	}

	public Boolean pesquisaFilhosDM_Parentes(LoginMBean mb, int comando) {
		StringBuilder sql = new StringBuilder();
		Boolean achou = false;
		SqlToBind resultado = new SqlToBind();

		try {

			this.filhos = new ArrayList<Parentes>();

			List<Integer> paginas = getPaginas(mb.getPaginaParentes(), comando, mb);
			Integer paginaInicial = paginas.get(0);
			Integer paginaFinal = paginas.get(1);
			mb.setPaginaParentes(paginas.get(2));
			String nomeMae = this.getInfocomplementares().getNomemae();
			String cpf = this.getInfocomplementares().getCpfcnpj();

			if (!estaVazioOuNulo(cpf)) {
				sql.delete(0, sql.length());
				sql.append(" select * from ( ");
				sql.append(" select pagina.*, ROWNUM PAGINA_RN  from ( ");
				sql.append(
						" select (SELECT STATUS  FROM TELEFONES_STATUS WHERE TELEFONE = T.TELEFONE AND ROWNUM <= 1) AS STATUS_LINHA,");
				sql.append(
						" (CASE WHEN (SELECT FLAG FROM TBL_WHATSAPP T2 WHERE T2.TELEFONE = T.TELEFONE AND ROWNUM <= 1) = 1 THEN 1 ELSE 0 END) AS WHATSAPP, ");
				sql.append(
						" (SELECT IS_DATE_CONFIRME(DT_OBITO) FROM OBITO WHERE NU_CPF = CPFCGC AND ROWNUM<=1 ) AS OBITO,");
				sql.append(
						" I.NOME,T.PROPRIETARIO AS PROPRIETARIO,T.ATUAL,T.TELEFONE,T.ENDERECO AS ENDERECO,T.NUMERO AS NUMERO,T.COMPLEMENTO,T.BAIRRO, ");
				sql.append(" T.CEP,T.CIDADE,T.UF,I.CPFCNPJ AS CPFCNPJ,");
				sql.append(
						" i.CPF_CONJUGE  AS CPF_CONJUGE,PEGA_NOME_INFO_COMP(I.CPF_CONJUGE) AS NOME_CONJUGE,I.NOME_MAE AS MAE, ");
				sql.append(
						" TO_CHAR(I.DATA_NASC,'DD/MM/YYYY') AS NASC,' ' AS TITULO,T.OPERADORA,TO_CHAR(T.DT_INSTALACAO,'YYYY-MM-DD') AS DT_INSTALACAO, ");
				sql.append(" I.SIGNO AS SIGNO, ");
				sql.append(" dom.GRPA_DS_PARENTESCO as GRPA_DS_PARENTESCO, dom.GRPA_DS_FORMA as GRPA_DS_FORMA ");
				sql.append(" FROM dm_parente pa, dm_grau_parentesco dom, telefones t, info_complementares i ");
				sql.append(" WHERE  pa.grpa_id_parentesco = DOM.GRPA_ID_PARENTESCO ");
				sql.append("  and pa.pare_cd_cpf_parente = T.CPFCGC (+) ");
				sql.append("  and pa.pare_cd_cpf_parente = I.CPFCNPJ ");
				sql.append("  and DOM.GRPA_ID_PARENTESCO = 3 "); // Filho
				sql.append("  and pa.pare_cd_cpf = ? ");
				sql.append("  AND ROWNUM <= ? ");
				sql.append("  ) PAGINA ) WHERE ( PAGINA_RN >= ? AND PAGINA_RN <= ? ) ");

				resultado.addString(cpf);
				resultado.addString(String.valueOf(br.com.confirmeonline.util.SQLConstantes.QTD_MAX_PESQUISA));
				resultado.addString(String.valueOf(paginaInicial));
				resultado.addString(String.valueOf(paginaFinal));

				resultado.setSql(sql.toString());

				achou = processaConsultaFilhos(resultado, 1, mb, mb.getQtdpesq());

				// if (achou == true) {
				// bPesquisaDM_Parente = true;
				// } else {
				// bPesquisaDM_Parente = false;
				// }

			}
			// mb.setM_parentes(achou);
			mb.setM_filhos(achou);

			String[] nomeservidor = mb.getServidor();
			// mb.getObjconexao().registraConsulta(conn, "PARENTE",
			// mb.getPessoaSite().getTelefone(), mb.getUsuario().getLogin(),
			// mb.getUsuario().getSenha(),
			// "CONFI",mb.getUsuario().getIP(),nomeservidor[2]);
		} catch (Exception e) {
			logger.error("Erro no metodo pesquisaFilhosDM_Parentes da classe Resposta: " + e.getMessage());
			achou = false;
			mb.setF_moradores(false);
			mb.setM_parentes(false);
		}
		String mensagem = "";
		if (!achou && this.exibirMensagem) {
			mensagem = "Não constam Parentes.";
		}
		FacesContext facesContext = FacesContext.getCurrentInstance();
		FacesMessage msg = new FacesMessage(mensagem);
		facesContext.addMessage("formResposta:searchButtonsError", msg);

		return achou;
	}

	public Boolean pesquisaMoradores(LoginMBean mb, int comando) {
		Integer pagina;
		StringBuilder sql = new StringBuilder();
		Boolean achou = false;
		Integer paginaFinal = null;
		Integer paginaInicial = null;

		this.moradores = new ArrayList<Moradores>();

		List<Integer> paginas = getPaginas(mb.getPaginaMoradores(), comando, mb);
		paginaInicial = paginas.get(0);
		paginaFinal = paginas.get(1);
		pagina = paginas.get(2);
		mb.setPaginaMoradores(pagina);
		try {

			Integer regra = 1;

			if (this.getTelefone().get(0).getComplemento().length() > 0) {

				if (this.getTelefone().get(0).getComplemento().substring(0, 1).equals("B")) {

					regra = 1;

				} else {

					regra = 2;
				}

			}

			achou = processaConsultaMoradores(new SqlToBind(), 1, mb, regra, this.getTelefone().get(0), paginaInicial,
					paginaFinal);

			mb.setM_moradores(achou);

			String[] nomeservidor = mb.getServidor();
			// mb.getObjconexao().registraConsulta(conn,
			// "MORADOR",this.getTelefone().get(0).getCep() ,
			// mb.getUsuario().getLogin(), mb.getUsuario().getSenha(),
			// "CONFI",mb.getUsuario().getIP(),nomeservidor[2]);
			String mensagem = "";
			if (!achou && this.exibirMensagem) {
				mensagem = "Não constam Moradores.";
			}
			FacesContext facesContext = FacesContext.getCurrentInstance();
			FacesMessage msg = new FacesMessage(mensagem);
			facesContext.addMessage("formResposta:searchButtonsError", msg);
		} catch (Exception ignore) {
		}
		return achou;

	}

	public Boolean pesquisaSocios(LoginMBean mb, int comando) {
		String qtdpesq = mb.getQtdpesq();
		StringBuilder sql = new StringBuilder();
		StringBuilder sql2 = new StringBuilder();
		Boolean achou = false;
		Integer pagina;
		SqlToBind resultado = new SqlToBind();

		try {
			if (mb.getPessoaSite().getCpfcnpj().length() == 11) {
				pagina = mb.getPaginaSociedades();
			} else {
				pagina = mb.getPaginaSocios();
			}
			List<Integer> paginas = getPaginas(pagina, comando, mb);
			Integer paginaInicial = paginas.get(0);
			Integer paginaFinal = paginas.get(1);
			pagina = paginas.get(2);

			if (mb.getPessoaSite().getCpfcnpj().length() == 11) {
				mb.setPaginaSociedades(pagina);
			} else {
				mb.setPaginaSocios(pagina);
			}

			if (mb.getPessoaSite().getCpfcnpj().length() == 11) {
				sql.delete(0, sql.length());
				sql.append("SELECT * FROM ( ");
				sql.append(" SELECT PAGINA.*,ROWNUM PAGINA_RN FROM ( ");
				sql.append(" SELECT * FROM ( ");
				sql.append(
						" SELECT S.NOME,S.CPF,(SELECT STATUS FROM TELEFONES_STATUS WHERE TELEFONE = T.TELEFONE AND ROWNUM <= 1) AS STATUS_LINHA, ");
				sql.append(
						" (CASE WHEN (SELECT FLAG FROM TBL_WHATSAPP T2 WHERE T2.TELEFONE = T.TELEFONE AND ROWNUM <= 1) = 1 THEN 1 ELSE 0 END) AS WHATSAPP, ");
				sql.append(
						" T.OPERADORA,TO_CHAR(T.DT_INSTALACAO,'DD/MM/YYYY') AS DT_INSTALACAO,T.PROPRIETARIO,T.ENDERECO,T.NUMERO,T.COMPLEMENTO,T.BAIRRO,T.CIDADE,T.CEP,T.UF,T.ATUAL,T.TELEFONE,E.CNPJ,E.RAZAO_SOCIAL,E.FANTASIA,E.ENDERECO AS ENDERECO_EMPRESA");
				sql.append(
						" ,E.NUMERO NUMERO_EMPRESA,E.COMPLEMENTO AS COMPLEMENTO_EMPRESA,E.BAIRRO AS BAIRRO_EMPRESA,E.CIDADE AS CIDADE_EMPRESA ");
				sql.append(
						" ,E.UF AS UF_EMPRESA,TO_CHAR(TO_DATE(E.DT_ABERTURA,'DD/MM/YYYY'),'DD/MM/YYYY') AS DT_ABERTURA,E.RAMO_ATVI,E.DESC_RAMO,E.DESC_NATUREZA,E.SITUACAO,S.CARGO,S.PARTIC,TO_CHAR(S.ENTRADA,'DD/MM/YYYY') AS ENTRADA");
				sql.append(
						" FROM QSA_EMPRESAS E,QSA_SOCIOS S, TELEFONES T WHERE E.CNPJ = S.CNPJ AND CAST(S.CNPJ AS  VARCHAR2(14)) = T.CPFCGC(+) ");
				sql.append(" AND S.CPF = ? AND ROWNUM <= ? ORDER BY CNPJ,TO_NUMBER(ATUAL) DESC ");
				sql.append(" ) ORDER BY CNPJ,TO_NUMBER(ATUAL) ) PAGINA ) ");
				sql.append(
						" WHERE ( PAGINA_RN >= ? AND PAGINA_RN <= ? )  ORDER BY CNPJ,TO_NUMBER(ATUAL) DESC, WHATSAPP DESC ");

				this.sociedades = new ArrayList<Sociedades>();

				resultado.limpaLista();
				resultado.addString(mb.getPessoaSite().getCpfcnpj());
				resultado.addString(String.valueOf(br.com.confirmeonline.util.SQLConstantes.QTD_MAX_PESQUISA));
				resultado.addString(String.valueOf(paginaInicial));
				resultado.addString(String.valueOf(paginaFinal));

				resultado.setSql(sql.toString());

				achou = processaConsultaSociedades(resultado, 1, mb);

				if (!achou) {
					achou = processaConsultaSociedades(new SqlToBind(), 1, mb);
				}
				mb.setM_sociedades(achou);
				mb.setM_socios(false);
			}

			if (mb.getPessoaSite().getCpfcnpj().length() == 14) {
				sql.delete(0, sql.length());
				sql.append("SELECT * FROM ( ");
				sql.append("SELECT PAGINA.*,ROWNUM PAGINA_RN FROM ( ");
				sql.append("SELECT * FROM ( ");
				sql.append(" SELECT (SELECT RAZAO_SOCIAL FROM QSA_EMPRESAS WHERE CNPJ = ? "
						+ "AND ROWNUM = 1) AS RAZAO_SOCIAL,CNPJ,CPF,NOME,CARGO,PARTIC,DATA,TO_CHAR(ENTRADA,'YYYY-MM-DD')"
						+ " AS ENTRADA FROM QSA_SOCIOS WHERE CNPJ = ? AND ROWNUM <= ?  ");
				sql.append(")  ) PAGINA ) ");
				sql.append("WHERE ( PAGINA_RN >= ? AND PAGINA_RN <= ? )  ");

				this.socios = new ArrayList<Socios>();

				resultado.limpaLista();
				resultado.addString(mb.getPessoaSite().getCpfcnpj());
				resultado.addString(mb.getPessoaSite().getCpfcnpj());
				resultado.addString(String.valueOf(br.com.confirmeonline.util.SQLConstantes.QTD_MAX_PESQUISA));
				resultado.addString(String.valueOf(paginaInicial));
				resultado.addString(String.valueOf(paginaFinal));

				resultado.setSql(sql.toString());

				achou = processaConsultaSocios(resultado, 1, mb);

				if (achou == false) {
					sql.delete(0, sql.length());
					sql.append("SELECT * FROM ( SELECT PAGINA.*,ROWNUM PAGINA_RN FROM ( ");
					sql.append("SELECT * FROM (  SELECT (SELECT RAZAO_SOCIAL FROM QSA_EMPRESAS WHERE CNPJ = ?");
					sql.append(
							"AND ROWNUM = 1) AS RAZAO_SOCIAL,CNPJ,'' AS CPF,FANTASIA AS NOME,'' AS CARGO,'' AS PARTIC,DT_ABERTURA AS DATA,''  AS ENTRADA,ENDERECO,NUMERO,COMPLEMENTO,BAIRRO,CIDADE,UF,SITUACAO,DESC_NATUREZA FROM QSA_EMPRESAS ");
					sql.append("WHERE CNPJ = ? AND ROWNUM <= ?)  "
							+ ") PAGINA ) WHERE ( PAGINA_RN >= ? AND PAGINA_RN <= ?)");

					resultado.limpaLista();
					resultado.addString(mb.getPessoaSite().getCpfcnpj());
					resultado.addString(mb.getPessoaSite().getCpfcnpj());
					resultado.addString(String.valueOf(br.com.confirmeonline.util.SQLConstantes.QTD_MAX_PESQUISA));
					resultado.addString(String.valueOf(paginaInicial));
					resultado.addString(String.valueOf(paginaFinal));

					resultado.setSql(sql.toString());

					achou = processaConsultaSociosEnder(resultado, 1, mb);
				}

				String[] nomeservidor = mb.getServidor();
				// mb.getObjconexao().registraConsulta(conn, "SOCIEDADES",
				// mb.getPessoaSite().getCpfcnpj(), mb.getUsuario().getLogin(),
				// mb.getUsuario().getSenha(),
				// "CONFI",mb.getUsuario().getIP(),nomeservidor[2]);
				mb.setM_sociedades(false);
				mb.setM_socios(achou);

			}
			String mensagem = "";
			if (!achou && this.exibirMensagem) {
				mensagem = "Não constam Sócios/Sociedades.";
			}
			FacesContext facesContext = FacesContext.getCurrentInstance();
			FacesMessage msg = new FacesMessage(mensagem);
			facesContext.addMessage("formResposta:searchButtonsError", msg);
		} catch (Exception ignore) {
			logger.error("Erro no metodo pesquisaSocios da classe resposta: " + ignore.getMessage());
		}

		return achou;
	}

	public Boolean pesquisaSocios2(LoginMBean mb, int comando) {
		String qtdpesq = mb.getQtdpesq();
		StringBuilder sql = new StringBuilder();
		StringBuilder sql2 = new StringBuilder();
		Boolean achou = false;
		Integer pagina;
		SqlToBind resultado = new SqlToBind();

		try {
			if (mb.getPessoaSite().getCpfcnpj().length() == 11) {
				pagina = mb.getPaginaSociedades();
			} else {
				pagina = mb.getPaginaSocios();
			}
			List<Integer> paginas = getPaginas(pagina, comando, mb);
			Integer paginaInicial = paginas.get(0);
			Integer paginaFinal = paginas.get(1);
			pagina = paginas.get(2);

			if (mb.getPessoaSite().getCpfcnpj().length() == 11) {
				mb.setPaginaSociedades(pagina);
			} else {
				mb.setPaginaSocios(pagina);
			}

			// if ( mb.getPessoaSite().getCpfcnpj().length() == 11 ){
			// sql.delete(0, sql.length());
			// sql.append("SELECT * FROM ( ");
			// sql.append(" SELECT PAGINA.*,ROWNUM PAGINA_RN FROM ( ");
			// sql.append(" SELECT * FROM ( ");
			// sql.append(" SELECT S.NOME,S.CPF,(SELECT STATUS FROM
			// TELEFONES_STATUS WHERE TELEFONE = T.TELEFONE AND ROWNUM <= 1) AS
			// STATUS_LINHA, ");
			// sql.append(" T.OPERADORA,TO_CHAR(T.DT_INSTALACAO,'DD/MM/YYYY') AS
			// DT_INSTALACAO,T.PROPRIETARIO,T.ENDERECO,T.NUMERO,T.COMPLEMENTO,T.BAIRRO,T.CIDADE,T.CEP,T.UF,T.ATUAL,T.TELEFONE,E.CNPJ,E.RAZAO_SOCIAL,E.FANTASIA,E.ENDERECO
			// AS ENDERECO_EMPRESA");
			// sql.append(" ,E.NUMERO NUMERO_EMPRESA,E.COMPLEMENTO AS
			// COMPLEMENTO_EMPRESA,E.BAIRRO AS BAIRRO_EMPRESA,E.CIDADE AS
			// CIDADE_EMPRESA ");
			// sql.append(" ,E.UF AS
			// UF_EMPRESA,TO_CHAR(TO_DATE(E.DT_ABERTURA,'DD/MM/YYYY'),'DD/MM/YYYY')
			// AS
			// DT_ABERTURA,E.RAMO_ATVI,E.DESC_RAMO,E.DESC_NATUREZA,E.SITUACAO,S.CARGO,S.PARTIC,TO_CHAR(S.ENTRADA,'DD/MM/YYYY')
			// AS ENTRADA");
			// sql.append(" FROM QSA_EMPRESAS E,QSA_SOCIOS S, TELEFONES T WHERE
			// E.CNPJ = S.CNPJ AND CAST(S.CNPJ AS VARCHAR2(14)) = T.CPFCGC(+)
			// ");
			// sql.append(" AND S.CPF = '"+ mb.getPessoaSite().getCpfcnpj() +"'
			// AND ROWNUM <=
			// "+br.com.confirmeonline.util.SQLConstantes.QTD_MAX_PESQUISA+"
			// ORDER BY CNPJ,TO_NUMBER(ATUAL) DESC ");
			// sql.append(" ) ORDER BY CNPJ,TO_NUMBER(ATUAL) ) PAGINA ) ");
			// sql.append(" WHERE ( PAGINA_RN >= '"+ paginaInicial +"' AND
			// PAGINA_RN <= '"+ paginaFinal +"' ) ORDER BY CNPJ,TO_NUMBER(ATUAL)
			// DESC ");
			//
			// this.sociedades= new ArrayList<Sociedades>();
			//
			// achou = processaConsultaSociedades(sql.toString(),1,mb);
			//
			// if(!achou){
			// achou = processaConsultaSociedades(sql2.toString(),1,mb);
			// }
			// mb.setM_sociedades(achou);
			// mb.setM_socios(false);
			// }

			sql.delete(0, sql.length());
			sql.append("SELECT * FROM ( ");
			sql.append("SELECT PAGINA.*,ROWNUM PAGINA_RN FROM ( ");
			sql.append("SELECT * FROM ( ");
			sql.append(
					" SELECT (SELECT RAZAO_SOCIAL FROM QSA_EMPRESAS WHERE CNPJ = Q.CNPJ AND ROWNUM = 1) AS RAZAO_SOCIAL,CNPJ,CPF,NOME,carg_ds_cargo,to_number(PARTIC) PARTIC,DATA,ENTRADA FROM QSA_SOCIOS Q,dm_cargo t2 ");

			int tamanhoCpfCnpj = mb.getPessoaSite().getCpfcnpj().trim().length();

			if (tamanhoCpfCnpj == 11) {
				sql.append(" WHERE CPF = ? ");
			} else if (tamanhoCpfCnpj == 14) {
				sql.append(" WHERE CNPJ = ? ");
			}

			sql.append("AND carg_id_cargo = cargo AND ROWNUM <= ? ");
			sql.append(")  ) PAGINA ) ");
			sql.append("WHERE ( PAGINA_RN >= + AND PAGINA_RN <= ? )  ");

			this.socios = new ArrayList<Socios>();

			resultado.limpaLista();
			resultado.addString(mb.getPessoaSite().getCpfcnpj());
			resultado.addString(String.valueOf(br.com.confirmeonline.util.SQLConstantes.QTD_MAX_PESQUISA));
			resultado.addString(String.valueOf(paginaInicial));
			resultado.addString(String.valueOf(paginaFinal));

			resultado.setSql(sql.toString());

			achou = processaConsultaSocios2(resultado, 1, mb);

			if (achou == false) {
				sql.delete(0, sql.length());
				sql.append("SELECT * FROM ( SELECT PAGINA.*,ROWNUM PAGINA_RN FROM ( ");
				sql.append(
						"SELECT * FROM (  SELECT RAZAO_SOCIAL,CNPJ,'' AS CPF,FANTASIA AS NOME,'' AS CARGO,'' AS PARTIC,DT_ABERTURA AS DATA,''  AS ENTRADA,ENDERECO,NUMERO,COMPLEMENTO,BAIRRO,CIDADE,UF,SITUACAO,DESC_NATUREZA FROM QSA_EMPRESAS ");
				sql.append(
						"WHERE CNPJ = ? AND ROWNUM <= ?)  " + ") PAGINA ) WHERE ( PAGINA_RN >= ? AND PAGINA_RN <= ? )");

				resultado.limpaLista();
				resultado.addString(mb.getPessoaSite().getCpfcnpj());
				resultado.addString(String.valueOf(br.com.confirmeonline.util.SQLConstantes.QTD_MAX_PESQUISA));
				resultado.addString(String.valueOf(paginaInicial));
				resultado.addString(String.valueOf(paginaFinal));

				resultado.setSql(sql.toString());

				achou = processaConsultaSociosEnder(resultado, 1, mb);
			}

			String[] nomeservidor = mb.getServidor();
			// mb.getObjconexao().registraConsulta(conn, "SOCIEDADES",
			// mb.getPessoaSite().getCpfcnpj(), mb.getUsuario().getLogin(),
			// mb.getUsuario().getSenha(),
			// "CONFI",mb.getUsuario().getIP(),nomeservidor[2]);
			mb.setM_sociedades(false);
			mb.setM_socios(achou);

			String mensagem = "";
			if (!achou && this.exibirMensagem) {
				mensagem = "Não constam Sócios/Sociedades.";
				paginasociedadesProx = false;
			} else {
				// Rodrigo Almeida
				if (socios.size() > 1 && pagina == 1 && (socios.size() > Integer.valueOf(qtdpesq))) {

					paginasociedadesAnt = false;
					paginasociedadesProx = true;
				}

				// Rodrigo Almeida
				if (socios.size() > 1 && pagina > 1) {

					paginasociedadesAnt = true;
					paginasociedadesProx = true;
				}

				// Rodrigo Almeida
				if (socios.size() > 1 && pagina > 1 && (socios.size() < Integer.valueOf(qtdpesq))) {

					paginasociedadesAnt = true;
					paginasociedadesProx = false;
				}

			}

			FacesContext facesContext = FacesContext.getCurrentInstance();
			FacesMessage msg = new FacesMessage(mensagem);
			facesContext.addMessage("formResposta:searchButtonsError", msg);
		} catch (Exception ignore) {

		}

		return achou;
	}

	public Boolean pesquisaEnderecosComerciais(LoginMBean mb, int comando) {
		Boolean achou = false;
		Integer pagina;

		try {
			pagina = mb.getPaginaEnderecosComerciais();
			List<Integer> paginas = getPaginas(pagina, comando, mb);
			Integer paginaInicial = paginas.get(0);
			Integer paginaFinal = paginas.get(1);
			pagina = paginas.get(2);

			mb.setPaginaEnderecosComerciais(pagina);

			this.enderecosComerciais = new ArrayList<EnderecoComercial>();

			achou = processaConsultaEnderecosComerciais(1, mb, paginaInicial, paginaFinal);

			String[] nomeservidor = mb.getServidor();
			// mb.getObjconexao().registraConsulta(conn, "SOCIEDADES",
			// mb.getPessoaSite().getCpfcnpj(), mb.getUsuario().getLogin(),
			// mb.getUsuario().getSenha(),
			// "CONFI",mb.getUsuario().getIP(),nomeservidor[2]);
			mb.setM_enderecosComercias(achou);

			String mensagem = "";
			if (!achou && this.exibirMensagem) {
				mensagem = "Não constam Enderecos Comerciais.";
			}
			FacesContext facesContext = FacesContext.getCurrentInstance();
			FacesMessage msg = new FacesMessage(mensagem);
			facesContext.addMessage("formResposta:searchButtonsError", msg);
		} catch (Exception ignore) {

		}

		return achou;
	}

	public Boolean pesquisaObito(LoginMBean mb, int comando) {
		StringBuilder sql = new StringBuilder();
		Boolean achou = false;
		try {
			this.obito = null;
			sql.delete(0, sql.length());
			sql.append(
					" select NU_LIVRO , NU_FOLHA ,   NU_TERMO  ,   TO_CHAR(TO_DATE(DT_LAVRAT,'YYYYMMDD'),'DD/MM/YYYY') AS DT_LAVRAT,         ");
			sql.append(
					"   NU_NB , NM_FALECIDO ,   NM_MAE_FALECIDO  ,   TO_CHAR(TO_DATE(DT_NASC,'YYYYMMDD'),'DD/MM/YYYY') AS DT_NASC  ,          ");
			sql.append(
					"   IS_DATE_CONFIRME(DT_OBITO) AS DT_OBITO  ,   NU_CPF ,   NIT ,   CS_IDENT ,  ID_CARTORIO   ,     ");
			sql.append("   NM_CARTORIO ,  ENDERECO_CARTORIO ,   CIDADE ,   NU_CEP   , BAIRRO ");
			sql.append(" from obito ");
			sql.append("  where nu_cpf = '" + mb.getPessoaSite().getCpfcnpj() + "' ");

			achou = processaConsultaObito(sql.toString(), mb);
			mb.setM_obito(achou);
			String[] nomeservidor = mb.getServidor();
			// mb.getObjconexao().registraConsulta(conn, "OBITO",
			// mb.getPessoaSite().getCpfcnpj(), mb.getUsuario().getLogin(),
			// mb.getUsuario().getSenha(),
			// "CONFI",mb.getUsuario().getIP(),nomeservidor[2]);
			String mensagem = "";
			if (!achou && this.exibirMensagem) {
				mensagem = "Não consta óbito.";
			}
			FacesContext facesContext = FacesContext.getCurrentInstance();
			FacesMessage msg = new FacesMessage(mensagem);
			facesContext.addMessage("formResposta:searchButtonsError", msg);
		} catch (Exception ignore) {

		}

		return achou;

	}

	public Boolean pesquisaObitoCompleto(LoginMBean mb, int comando) {
		StringBuilder sql = new StringBuilder();
		Boolean achou = false;
		try {
			this.obito = null;
			sql.delete(0, sql.length());
			sql.append(
					" select NU_LIVRO , NU_FOLHA ,   NU_TERMO  ,   TO_CHAR(TO_DATE(DT_LAVRAT,'YYYYMMDD'),'DD/MM/YYYY') AS DT_LAVRAT,         ");
			sql.append(
					"   NU_NB , NM_FALECIDO ,   NM_MAE_FALECIDO  ,   TO_CHAR(TO_DATE(DT_NASC,'YYYYMMDD'),'DD/MM/YYYY') AS DT_NASC  ,          ");
			sql.append(
					"   IS_DATE_CONFIRME(DT_OBITO) AS DT_OBITO  ,   NU_CPF ,   NIT ,   CS_IDENT ,  ID_CARTORIO   ,     ");
			sql.append("   NM_CARTORIO ,  ENDERECO_CARTORIO ,   CIDADE ,   NU_CEP   , BAIRRO ");
			sql.append(" from obito_completo");
			sql.append("  where nu_cpf = '" + mb.getPessoaSite().getCpfcnpj() + "' AND NM_FALECIDO='"
					+ this.infocomplementares.getNome() + "' AND NOT REMOVIDO =1");

			achou = processaConsultaObito(sql.toString(), mb);
			mb.setM_obito(achou);
			String[] nomeservidor = mb.getServidor();
			// mb.getObjconexao().registraConsulta(conn, "OBITO",
			// mb.getPessoaSite().getCpfcnpj(), mb.getUsuario().getLogin(),
			// mb.getUsuario().getSenha(),
			// "CONFI",mb.getUsuario().getIP(),nomeservidor[2]);
			String mensagem = "";
			if (!achou && this.exibirMensagem) {
				mensagem = "Não consta óbito.";
			}
			FacesContext facesContext = FacesContext.getCurrentInstance();
			FacesMessage msg = new FacesMessage(mensagem);
			facesContext.addMessage("formResposta:searchButtonsError", msg);
		} catch (Exception ignore) {

		}

		return achou;

	}

	public Boolean pesquisaEmail(LoginMBean mb, int comando) {
		Boolean achou = false;
		try {
			this.emails = new ArrayList<Emails>();

			achou = processaEmail(comando, mb);
			mb.setM_email(achou);
			String mensagem = "";
			if (!achou && this.exibirMensagem) {
				mensagem = "Não constam emails.";
			}
			FacesContext facesContext = FacesContext.getCurrentInstance();
			FacesMessage msg = new FacesMessage(mensagem);
			facesContext.addMessage("formResposta:searchButtonsError", msg);
		} catch (Exception ignore) {

		}

		return achou;
	}

	public Boolean pesquisaTelComercial(LoginMBean mb, int comando) {
		StringBuilder sql = new StringBuilder();
		SqlToBind resultado = new SqlToBind();
		Boolean achou = false;

		try {
			List<Integer> paginas = getPaginas(mb.getPaginaTelCom(), comando, mb);
			Integer paginaInicial = paginas.get(0);
			Integer paginaFinal = paginas.get(1);
			mb.setPaginaTelCom(paginas.get(2));
			/* Processamento de Tefones Comerciais */

			if (mb.getUsuario().getPossuitelefonecomercial() == true && menorDeIdade==false) {

				this.telefoneComercial = new ArrayList<TelefonesComerciais>();
				sql.delete(0, sql.length());
				sql.append("SELECT * ");
				sql.append(
						"FROM (SELECT PAGINA.*,ROWNUM PAGINA_RN FROM ( SELECT (SELECT STATUS FROM TELEFONES_STATUS WHERE TELEFONE = T.TELEFONE AND ROWNUM <= 1) AS STATUS_LINHA,");
				sql.append(
						" (CASE WHEN (SELECT FLAG FROM TBL_WHATSAPP T2 WHERE T2.TELEFONE = T.TELEFONE AND ROWNUM <= 1) = 1 THEN 1 ELSE 0 END) AS WHATSAPP, ");
				sql.append(
						"(SELECT DT_ABERTURA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS DT_ABERTURA,");
				sql.append("(SELECT SITUACAO FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS SITUACAO,");
				sql.append("(SELECT FANTASIA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS FANTASIA,");
				sql.append(
						"(SELECT DESC_NATUREZA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS NATUREZA,");
				sql.append("T.PROPRIETARIO AS PROPRIETARIO,");
				sql.append("T.ATUAL,");
				sql.append("T.TELEFONE,");
				sql.append("T.ENDERECO AS ENDERECO,");
				sql.append("T.NUMERO AS NUMERO,");
				sql.append("T.COMPLEMENTO,");
				sql.append("T.BAIRRO,");
				sql.append("T.CEP,");
				sql.append("T.CIDADE,");
				sql.append("T.UF,");
				sql.append("T.CPFCGC AS CPFCNPJ,");
				sql.append("' ' AS TITULO,");
				sql.append("T.OPERADORA,");
				sql.append("TO_CHAR(T.DT_INSTALACAO,'YYYY-MM-DD') AS DT_INSTALACAO ");
				sql.append("FROM TELEFONES T,TELEFONES_COMERCIAIS C ");
				sql.append("WHERE T.CPFCGC = C.CNPJ ");
				sql.append("AND C.CPF = ? ");
				sql.append("AND ROWNUM <= ? ");
				sql.append(
						"ORDER BY da_desligamento_rais_dm asc , da_admissao_rais_dma desc , TO_NUMBER(ATUAL) DESC ) PAGINA ) ");
				sql.append(
						"WHERE ( PAGINA_RN >= ? AND PAGINA_RN <= ? ) ORDER BY TO_NUMBER(ATUAL) DESC, WHATSAPP DESC  ");

				resultado.limpaLista();
				resultado.addString(mb.getPessoaSite().getCpfcnpj());
				resultado.addString(String.valueOf(br.com.confirmeonline.util.SQLConstantes.QTD_MAX_PESQUISA));
				resultado.addString(String.valueOf(paginaInicial));
				resultado.addString(String.valueOf(paginaFinal));

				resultado.setSql(sql.toString());

				achou = processaConsultaTelefonesComerciais(resultado, 1, mb);
				mb.setM_tel_comercial(achou);

			} else {

				String respcomercial = Conexao.comercial(mb.getPessoaSite().getCpfcnpj(), this.getConnection(),
						mb.getUsuario().getPossuitelefonecomercial());
				this.setRespnaopossuitelcomercial(respcomercial);

			}
			String[] nomeservidor = mb.getServidor();
			// mb.getObjconexao().registraConsulta(conn, "COMERCIAL",
			// mb.getPessoaSite().getCpfcnpj(), mb.getUsuario().getLogin(),
			// mb.getUsuario().getSenha(),
			// "CONFI",mb.getUsuario().getIP(),nomeservidor[2]);
			String mensagem = "";
			if (!achou && this.exibirMensagem) {
				mensagem = "Não constam Telefones Comerciais.";
			}
			FacesContext facesContext = FacesContext.getCurrentInstance();
			FacesMessage msg = new FacesMessage(mensagem);
			facesContext.addMessage("formResposta:searchButtonsError", msg);
		} catch (Exception ignore) {
		} finally {
			releaseConnection();
		}
		return achou;
	}

	public Boolean pesquisaVeiculos(LoginMBean mb, int comando) {
		StringBuilder sql = new StringBuilder();
		Boolean achou = false;
		Integer posix = 0;
		this.veiculos = new ArrayList<Veiculo>();
		try {
			List<Integer> paginas = getPaginas(mb.getPaginaVeiculos(), comando, mb);
			Integer paginaInicial = paginas.get(0);
			Integer paginaFinal = paginas.get(1);
			mb.setPaginaVeiculos(paginas.get(2));

			sql.delete(0, sql.length());
			sql.append("SELECT * FROM ( ");
			sql.append(" SELECT PAGINA.*,ROWNUM PAGINA_RN FROM (  ");
			sql.append(" SELECT * FROM ( ");
			sql.append(" SELECT V.PLACA,V.MARCA,V.RENAVAN,V.ANOFAB,V.CHASSI,V.COMBU,V.ANOMODE,V.PROPRI, ");
			sql.append(" V.END,V.NUM,V.COMPL,V.BAIRRO,V.CEP,V.CPF AS CPFCNPJ,V.CIDADE,V.ESTADO,V.DAINCL,V.DALICE ");
			sql.append(" FROM VEICULOS V  ");
			sql.append(" WHERE V.CPF = '" + mb.getPessoaSite().getCpfcnpj() + "' AND ROWNUM <= "
					+ br.com.confirmeonline.util.SQLConstantes.QTD_MAX_PESQUISA + " ");
			sql.append(" )  ) PAGINA ) WHERE ( PAGINA_RN >= '" + paginaInicial + "' AND PAGINA_RN <= '" + paginaFinal
					+ "' ) ");

			achou = processaConsultaVeiculos(sql.toString(), 1, mb);
			mb.setM_veiculo(achou);
			// String [] nomeservidor = mb.getServidor();
			// mb.getObjconexao().registraConsulta(conn, "VEICULOS",
			// mb.getPessoaSite().getCpfcnpj(), mb.getUsuario().getLogin(),
			// mb.getUsuario().getSenha(),
			// "CONFI",mb.getUsuario().getIP(),nomeservidor[2]);
			String mensagem = "";
			if (!achou && this.exibirMensagem) {
				mensagem = "Não constam Veiculos.";
			}
			FacesContext facesContext = FacesContext.getCurrentInstance();
			FacesMessage msg = new FacesMessage(mensagem);
			facesContext.addMessage("formResposta:searchButtonsError", msg);
		} catch (Exception ignore) {

		}
		return achou;

	}

	public Boolean pesquisaTelRef(LoginMBean mb, int comando) {
		StringBuilder sql = new StringBuilder();
		Boolean achou = false;
		try {
			List<Integer> paginas = getPaginas(mb.getPaginaTelRef(), comando, mb);
			Integer paginaInicial = paginas.get(0);
			Integer paginaFinal = paginas.get(1);
			mb.setPaginaTelRef(paginas.get(2));

			this.telefoneReferencia = new ArrayList<TelefonesReferencia>();

			sql.append("SELECT * FROM ( ");
			sql.append(" SELECT PAGINA.*,ROWNUM PAGINA_RN FROM ( ");
			sql.append(
					" SELECT (SELECT STATUS FROM TELEFONES_STATUS WHERE TELEFONE = T.TELEFONE AND ROWNUM <= 1) AS STATUS_LINHA, ");
			sql.append(
					" (CASE WHEN (SELECT FLAG FROM TBL_WHATSAPP T2 WHERE T2.TELEFONE = T.TELEFONE AND ROWNUM <= 1) = 1 THEN 1 ELSE 0 END) AS WHATSAPP, ");
			sql.append(
					" (SELECT IS_DATE_CONFIRME(DT_OBITO) FROM OBITO WHERE NU_CPF = CPFCGC AND ROWNUM<=1 ) AS OBITO, ");
			sql.append(
					" (SELECT DT_ABERTURA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS DT_ABERTURA, ");
			sql.append(" (SELECT SITUACAO FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS SITUACAO,");
			sql.append(" (SELECT FANTASIA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS FANTASIA, ");
			sql.append(" (SELECT DESC_NATUREZA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS NATUREZA,");
			sql.append(
					" I.NOME,T.PROPRIETARIO AS PROPRIETARIO,T.ATUAL,T.TELEFONE,T.ENDERECO AS ENDERECO,T.NUMERO AS NUMERO,T.COMPLEMENTO,T.BAIRRO,T.CEP,T.CIDADE,T.UF,T.CPFCGC AS CPFCNPJ,");
			sql.append(
					" i.CPF_CONJUGE AS CPF_CONJUGE,PEGA_NOME_INFO_COMP(I.CPF_CONJUGE) AS NOME_CONJUGE,I.NOME_MAE AS MAE,TO_CHAR(I.DATA_NASC,'DD/MM/YYYY') AS NASC,' ' AS TITULO,T.OPERADORA,TO_CHAR(T.DT_INSTALACAO,'YYYY-MM-DD') AS DT_INSTALACAO, I.SIGNO AS SIGNO ");
			sql.append(" FROM DBCRED.TELEFONES T, DBCRED.INFO_COMPLEMENTARES I , DBCRED.TELEFONES_REFERENCIA R  ");
			sql.append(" WHERE  ");
			sql.append(" T.PROPRIETARIO IS NOT NULL      AND  ");
			sql.append(" T.TELEFONE = R.TELEFONE         AND ");
			sql.append(" T.CPFCGC = I.CPFCNPJ(+)         AND ");
			sql.append(" T.CPFCGC <> '" + mb.getPessoaSite().getCpfcnpj() + "'       AND  ");
			sql.append(" R.CPFCNPJ = '" + mb.getPessoaSite().getCpfcnpj() + "' AND ROWNUM <= "
					+ br.com.confirmeonline.util.SQLConstantes.QTD_MAX_PESQUISA
					+ " ORDER BY CPFCNPJ, TO_NUMBER(ATUAL) DESC, WHATSAPP DESC ");
			sql.append(" ) PAGINA ");
			// sql.append(" ) WHERE ( PAGINA_RN >= " + paginaInicial + " AND
			// PAGINA_RN <= "+ paginaFinal + " ) ORDER BY TO_NUMBER(ATUAL) DESC
			// ");
			sql.append(" ) WHERE  ( PAGINA_RN >=  " + paginaInicial + "  AND  PAGINA_RN <= " + paginaFinal + " )");

			achou = processaConsultaTelRefAux(sql.toString(), 1, mb);
			mb.setM_telefone_referencia(achou);
			String[] nomeservidor = mb.getServidor();

			// mb.getObjconexao().registraConsulta(conn, "REFERENCIA",
			// mb.getPessoaSite().getCpfcnpj(), mb.getUsuario().getLogin(),
			// mb.getUsuario().getSenha(),
			// "CONFI",mb.getUsuario().getIP(),nomeservidor[2]);
			String mensagem = "";
			if (!achou && this.exibirMensagem) {
				mensagem = "Não constam Telefones de Referência.";
			}
			FacesContext facesContext = FacesContext.getCurrentInstance();
			FacesMessage msg = new FacesMessage(mensagem);
			facesContext.addMessage("formResposta:searchButtonsError", msg);
		} catch (Exception ignore) {

		}
		return achou;
	}

	public Boolean pesquisaImoveis(LoginMBean mb, int comando) {
		StringBuilder sql = new StringBuilder();
		Boolean achou = false;
		try {
			this.imoveis = new ArrayList<Imoveis>();
			if (!estaVazioOuNulo(mb.getPessoaSite().getCpfcnpj())) {
				List<Integer> paginas = getPaginas(mb.getPaginaImoveis(), comando, mb);
				Integer paginaInicial = paginas.get(0);
				Integer paginaFinal = paginas.get(1);
				mb.setPaginaImoveis(paginas.get(2));

				sql.append(" SELECT * FROM ( ");
				sql.append(" SELECT PAGINA.*,ROWNUM PAGINA_RN FROM ( ");
				sql.append(
						" select i.CPF, i.NOME, i.ENDERECO, i.CEP, i.SETOR, i.QUADRA, i.LOTE, i.CODIGO_LOG, i.AREA_TERRENO, i.AREA_CONSTRUIDA, i.ANO_CONSTRUCAO,i.BASE_CALCULO, TO_CHAR(TO_DATE(i.DIA_VENCIMENTO,'DD/MM/YYYY'), 'DD/MM/YYYY') AS DIA_VENCIMENTO, i.TESTADA, i.FRACAO_IDEAL, i.INSCRICAO ");
				sql.append(" from iptu i where cpf ='" + mb.getPessoaSite().getCpfcnpj() + "' AND ROWNUM <= "
						+ br.com.confirmeonline.util.SQLConstantes.QTD_MAX_PESQUISA + " ");
				sql.append(" ) PAGINA ");
				sql.append(" ) WHERE  ( PAGINA_RN >= " + paginaInicial + " AND  PAGINA_RN <= " + paginaFinal + " ) ");

				achou = processaConsultaImovel(sql.toString(), 1, mb);
				mb.setM_imoveis(achou);
				String[] nomeservidor = mb.getServidor();
				// mb.getObjconexao().registraConsulta(conn, "IMOVEL",
				// mb.getPessoaSite().getCpfcnpj(), mb.getUsuario().getLogin(),
				// mb.getUsuario().getSenha(),
				// "CONFI",mb.getUsuario().getIP(),nomeservidor[2]);
				String mensagem = "";
				if (!achou && this.exibirMensagem) {
					mensagem = "Não constam Imóveis.";
				}
				FacesContext facesContext = FacesContext.getCurrentInstance();
				FacesMessage msg = new FacesMessage(mensagem);
				facesContext.addMessage("formResposta:searchButtonsError", msg);
			}
		} catch (Exception ignore) {

		}
		return achou;
	}

	public Boolean pesquisaConsultaCredito(int comando, LoginMBean mx) {
		/*
		 * Método de pesquisa de Crédito By SMarcio em 27/11/2013 Instancia o webservice
		 * de crédito que fica no servidor consulta2
		 */

		String xmlcredito = "";
		Boolean ok;
		String tbL = "<table width='100%'>";
		String L1 = "<tr><td class='acertacredito'bgColor='#F7F7F7' align='center'><font face='Verdana' size=3>";
		String L2 = "</font></td></tr>";
		String L3 = "<tr class='acertacredito'><td class='acertacredito' bgColor='#EEEEEE'><font face='Verdana' size=1>";
		String L4 = "<td class='acertacredito' bgColor='#F7F7F7' align='center'><font face='Verdana' size=1>";
		String tb = "<table aling='center' cellpadding=0 cellspacing=0 border=2 width='100%'>";
		String col = "<td class='acertacredito' bgColor='#F7F7F7' align='center'><font class='acerto' coface='Verdana' size=1>";
		String filtab = "--";
		String idtab = "--";
		String nasctab = "--";
		String sigtab = " ";
		int rpf = 0;
		int rpj = 0;
		String nomecomp = "--";
		String nomecompE = "--";
		String qtdalerta = "";
		String qtdbancarias = "";
		String qtdlojista = "";
		String qtdchequespre = "";
		String porteT = "";
		String impostoT = "";
		String totalfuncionariosT = "";
		String funcmaior5T = "";
		String funcmenor5T = "";
		String dtiregempT = "";
		String endempT = "";
		String bairroempT = "";
		String cidempT = "";
		String ufempT = "";
		String cepempT = "";
		String qtdbancos = "";
		String clientecredT;
		String datahoraT;
		String emissaoT;
		String bcocqT;
		String ncheqT;
		String vcocqT;
		String valcliT;
		String valchequeT;
		String valrendaT;
		String empresapagT;
		String credorT;
		String tipodocT;
		String valorT;
		String dtljT;
		String dtrT;
		String bcoT;
		String ageT;
		String qtdT;
		String dtiniciosocioT;
		String nomesocioT;
		String endsocioT;
		String cepsocioT;
		String bairrosocioT;
		String cidadesocioT;
		String ufsocioT;
		String msgalerta;
		String dtalerta;
		String msg;
		Node clientecred;
		Node datahora;
		Node dtiniciosocio;
		Node nomesocio;
		Node endsocio;
		Node cepsocio;
		Node bairrosocio;
		Node cidadesocio;
		Node ufsocio;
		NodeList conta;
		Node alertaaux;
		Node malerta;
		Node mdt;
		Node restricao;
		Node dtr;
		Node bco;
		Node age;
		Node qtd;
		Node ljaux;
		Node credor;
		Node tipodoc;
		Node valor;
		Node dtlj;
		Node cqaux;
		Node emissao;
		Node bcocq;
		Node ncheq;
		Node vcocq;
		Node valcli;
		Node valcheque;
		Node valrenda;
		Node empresapag;
		NodeList registropassagem;
		Element eElement;

		try {

			xmlcredito = ccredito(mx.getUsuario().getUsuario_credito(), mx.getUsuario().getSenha_credito(),
					mx.getPessoaSite().getCpfcnpj());

			xmlcredito = "<?xml version='1.0' encoding='ISO-8859-1'?>" + xmlcredito;

			/*
			 * monta a resposta do credito no bean para mostrar na pagina principal
			 */
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			ByteArrayInputStream bis = new ByteArrayInputStream(xmlcredito.getBytes());
			Document doc = db.parse(bis);
			

			
			
			NodeList cpfcnpjcredito = doc.getElementsByTagName("cpf");
			NodeList nomecompleto = doc.getElementsByTagName("nome_completo");
			NodeList infoRestricao = doc.getElementsByTagName("info_restricao");
			NodeList nome = doc.getElementsByTagName("nome_completo");
			NodeList filiacao = doc.getElementsByTagName("filiacao");
			NodeList identidade = doc.getElementsByTagName("identidade");
			NodeList datanascimento = doc.getElementsByTagName("data_nascimento");
			NodeList signo = doc.getElementsByTagName("signo");
			NodeList codigoconsulta = doc.getElementsByTagName("codigo_consulta");
			NodeList restricoeslojistas = doc.getElementsByTagName("restricoes_lojistas");
			NodeList restricoesbancarias = doc.getElementsByTagName("restricoes_bancarias");
			NodeList chequespredatados = doc.getElementsByTagName("cheques_pre_datados");
			NodeList alertas = doc.getElementsByTagName("alertas");
			NodeList consultabancos = doc.getElementsByTagName("consulta_bancos");
			NodeList rendapf = doc.getElementsByTagName("rendapf");
			NodeList rendapj = doc.getElementsByTagName("rendapj");
			NodeList empresacredito = doc.getElementsByTagName("empresa");
			NodeList socios = doc.getElementsByTagName("socios");
			NodeList consultasrealizadas = doc.getElementsByTagName("consultas_realizadas");
			NodeList passagem = doc.getElementsByTagName("passagem");
			List<String> telefonesProprietario = StringUtils.getXmlTagValue(xmlcredito,
					"consulta_telefone_proprietario", "numero_telefone");
			List<String> logradourosProprietario = StringUtils.getXmlTagValue(xmlcredito,
					"consulta_telefone_proprietario", "logradouro");
			List<String> bairrosProprietario = StringUtils.getXmlTagValue(xmlcredito, "consulta_telefone_proprietario",
					"bairro");
			List<String> cepsProprietario = StringUtils.getXmlTagValue(xmlcredito, "consulta_telefone_proprietario",
					"cep");
			List<String> cidadesProprietario = StringUtils.getXmlTagValue(xmlcredito, "consulta_telefone_proprietario",
					"cidade");
			List<String> ufsProprietario = StringUtils.getXmlTagValue(xmlcredito, "consulta_telefone_proprietario",
					"uf");
			List<String> proprietariosReferencia = StringUtils.getXmlTagValue(xmlcredito,
					"consulta_telefone_referencia", "proprietario");
			List<String> telefonesReferencia = StringUtils.getXmlTagValue(xmlcredito, "consulta_telefone_referencia",
					"numero_telefone");
			List<String> logradourosReferencia = StringUtils.getXmlTagValue(xmlcredito, "consulta_telefone_referencia",
					"logradouro");
			List<String> bairrosReferencia = StringUtils.getXmlTagValue(xmlcredito, "consulta_telefone_referencia",
					"bairro");
			List<String> cepsReferencia = StringUtils.getXmlTagValue(xmlcredito, "consulta_telefone_referencia", "cep");
			List<String> cidadesReferencia = StringUtils.getXmlTagValue(xmlcredito, "consulta_telefone_referencia",
					"cidade");
			List<String> ufsReferencia = StringUtils.getXmlTagValue(xmlcredito, "consulta_telefone_referencia", "uf");

//			Rodrigo Almeida - 31/03/2020 - Verificando se o registro pertence a um menor de idade
			if (datanascimento.getLength() > 0) {
				nasctab = datanascimento.item(0).getFirstChild().getNodeValue();
				
				String sDate1=nasctab;
				
				menorDeIdade=false;
				 
				SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yy");
				// output format: yyyy-MM-dd
				SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
			//	System.out.println(formatter.format(parser.parse(sDate1)));
				
				sDate1 = formatter.format(parser.parse(sDate1));
				 
				
				Integer idade = Utils.calculaIdade(sDate1);
					
				if (idade == null) {
					idade=18;
				}
					
					
				if (idade < 18 ) {
					mensagem="Menor de Idade";
					menorDeIdade=true;
				}
			}
			
			
			if (menorDeIdade==false) {
				
				// Monta Resposta em html para a div da pagina principal
	
				// Bancos
				if (consultabancos.getLength() > 0) {
	
					NodeList qtdbancosAux = consultabancos.item(0).getChildNodes();
					qtdbancos = qtdbancosAux.item(0).getFirstChild().getNodeValue();
	
				}
				// Cheques Pre
				if (chequespredatados.getLength() > 0) {
	
					NodeList qtdchequespreAux = chequespredatados.item(0).getChildNodes();
					qtdchequespre = qtdchequespreAux.item(0).getFirstChild().getNodeValue();
				}
	
				// Bancarias
				if (restricoesbancarias.getLength() > 0) {
	
					NodeList qtdbancariasAux = restricoesbancarias.item(0).getChildNodes();
					qtdbancarias = qtdbancariasAux.item(0).getFirstChild().getNodeValue();
	
				}
				// Lojistas
				if (restricoeslojistas.getLength() > 0) {
	
					NodeList qtdlojistaAux = restricoeslojistas.item(0).getChildNodes();
					qtdlojista = qtdlojistaAux.item(0).getFirstChild().getNodeValue();
				}
	
				// Alerta
	
				if (alertas.getLength() > 0) {
	
					msg = "NÃO EXISTEM ALERTAS PARA ESSE CPF/CNPJ";
					try {
						NodeList alerta = alertas.item(0).getChildNodes();
						qtdalerta = alerta.item(0).getFirstChild().getNodeValue();
						if (qtdalerta == null || Integer.parseInt(qtdalerta) <= 0) {
	
							msg = "NÃO EXISTEM ALERTAS PARA ESSE CPF/CNPJ";
	
						} else {
	
							msg = "";
	
						}
	
					} catch (Exception e) {
	
						// zebrou mais nao me ferrou
	
					}
	
				}
				// Renda PF
	
				if (rendapf.getLength() > 0) {
	
					NodeList seqrendapfl = rendapf.item(0).getChildNodes();
	
					if (seqrendapfl.getLength() > 0) {
	
						rpf = 1;
	
					}
	
				} else {
	
					rpf = 0;
				}
	
				// Renda PJ
				if (rendapj.getLength() > 0) {
	
					rpj = 1;
					/*
					 * Node porte = rendapj.item(0).getAttributes().getNamedItem("porte"); Node
					 * imposto = rendapj.item(0).getAttributes().getNamedItem("tipoimposto"); Node
					 * totalfuncionarios = rendapj.item(0).getAttributes().getNamedItem(
					 * "totalfuncionarios"); Node funcmaior5 =
					 * rendapj.item(0).getAttributes().getNamedItem( "qtdfuncmenor5sal"); Node
					 * funcmenor5 = rendapj.item(0).getAttributes().getNamedItem(
					 * "qtdfuncmenor5sal");
					 */
					porteT = rendapj.item(0).getChildNodes().item(1).getTextContent();
					impostoT = rendapj.item(0).getChildNodes().item(2).getTextContent();
					totalfuncionariosT = rendapj.item(0).getChildNodes().item(3).getTextContent();
					funcmaior5T = rendapj.item(0).getChildNodes().item(4).getTextContent();
					funcmenor5T = rendapj.item(0).getChildNodes().item(5).getTextContent();
	
				} else {
					rpj = 0;
				}
	
				// Nome completo
				if (nomecompleto.getLength() > 0) {
	
					try {
	
						nomecomp = nomecompleto.item(0).getFirstChild().getNodeValue();
	
					} catch (Exception e) {
	
						nomecomp = "";
	
					}
	
				}
	
				// Filiacao
				if (filiacao.getLength() > 0) {
	
					try {
	
						filtab = filiacao.item(0).getFirstChild().getNodeValue();
					} catch (Exception e) {
	
						filtab = "";
	
					}
	
				}
	
				// Identidade
				if (identidade.getLength() > 0) {
	
					try {
	
						idtab = identidade.item(0).getFirstChild().getNodeValue();
	
					} catch (Exception e) {
	
						idtab = "";
	
					}
	
				}
	
				// Data Nascinento
	
				if (datanascimento.getLength() > 0) {
	
					try {
	
						nasctab = datanascimento.item(0).getFirstChild().getNodeValue();
	
					} catch (Exception e) {
	
						nasctab = "";
	
					}
	
				}
	
				// Signo
	
				if (signo.getLength() > 0) {
	
					try {
	
						sigtab = signo.item(0).getFirstChild().getNodeValue();
						if (sigtab == null) {
	
							sigtab = "";
						}
	
					} catch (Exception e) {
	
						sigtab = "";
	
					}
	
				}
	
				// Empresa nm
	
				if (empresacredito.getLength() > 0) {
	
					// Node nomeempresa =
					// empresacredito.item(0).getAttributes().getNamedItem("razaosocial");
	
					if (empresacredito.item(0).getChildNodes().item(1).getTextContent() != null) {
	
						nomecompE = empresacredito.item(0).getChildNodes().item(1).getTextContent();
	
					}
	
					// nomecompE = nomecompleto[0].childNodes[0].nodeValue;
	
					/*
					 * Node dtiregemp = empresacredito.item(0).getAttributes().getNamedItem(
					 * "dt.inicio"); Node endemp =
					 * empresacredito.item(0).getAttributes().getNamedItem( "endereco"); Node
					 * bairroemp = empresacredito.item(0).getAttributes().getNamedItem("bairro") ;
					 * Node cidemp = empresacredito.item(0).getAttributes().getNamedItem("cidade") ;
					 * Node ufemp = empresacredito.item(0).getAttributes().getNamedItem("uf"); Node
					 * cepemp = empresacredito.item(0).getAttributes().getNamedItem("cep");
					 */
	
					if (empresacredito.item(0).getChildNodes().item(0).getTextContent() != null
							&& !empresacredito.item(0).getChildNodes().item(0).getTextContent().equals("")) {
	
						dtiregempT = empresacredito.item(0).getChildNodes().item(0).getTextContent();
	
					} else {
	
						dtiregempT = "XXXXXXXX";
	
					}
	
					if (empresacredito.item(0).getChildNodes().item(2).getTextContent() != null) {
	
						endempT = empresacredito.item(0).getChildNodes().item(2).getTextContent();
	
					} else {
	
						endempT = "--";
	
					}
	
					if (empresacredito.item(0).getChildNodes().item(3).getTextContent() != null) {
	
						bairroempT = empresacredito.item(0).getChildNodes().item(3).getTextContent();
	
					} else {
	
						bairroempT = "--";
	
					}
	
					if (empresacredito.item(0).getChildNodes().item(4).getTextContent() != null) {
	
						cidempT = empresacredito.item(0).getChildNodes().item(4).getTextContent();
	
					} else {
	
						cidempT = "--";
	
					}
	
					if (empresacredito.item(0).getChildNodes().item(6).getTextContent() != null) {
	
						ufempT = empresacredito.item(0).getChildNodes().item(6).getTextContent();
	
					} else {
	
						ufempT = "--";
	
					}
					if (empresacredito.item(0).getChildNodes().item(5).getTextContent() != null) {
	
						cepempT = empresacredito.item(0).getChildNodes().item(5).getTextContent();
	
					} else {
	
						cepempT = "--";
	
					}
	
				}
	
				// Informacoes Gerais
				tbL = tbL + "<br>";

				String restricaoStatus = "CPF/CNPJ COM RESTRICAO";
				if (infoRestricaoOk(infoRestricao, restricaoStatus)) {
	
					tbL = tbL
							+ "<tr><td><font class='corcredito' color='#FFFFFF' face='Verdana' size='4'><marquee BEHAVIOR=alternate bgcolor='#FF0000'>ATENÇÃO - CPF/CNPJ COM RESTRIÇÃO</marquee>"
							+ L2;
	
				}

				restricaoStatus = "* NADA CONSTA *";
				if (infoRestricaoOk(infoRestricao, restricaoStatus)) {
	
					tbL = tbL
							+ "<tr><td><font class='corcredito' face='Verdana' size='4'><marquee BEHAVIOR=alternate bgcolor='#009933'>NADA CONSTA</marquee>"
							+ L2;
	
				}
	
				if (qtdalerta.equals("") || qtdalerta.equals(null)) {
	
					qtdalerta = "0";
	
				}
	
				if (Integer.parseInt(qtdalerta) > 0) {
	
					tbL = tbL
							+ "<tr><td><font class='corcredito' color='#FFFFFF' face='Verdana' size='4'><marquee BEHAVIOR=alternate bgcolor='#FFCC66'>POSSUI ALERTA</marquee>"
							+ L2;
	
				}

				String strCpfCnpjCredito = cpfcnpjcredito.item(0).getFirstChild() != null ? cpfcnpjcredito.item(0).getFirstChild().getNodeValue() : "";
				String codigoDaConsulta = codigoconsulta.item(0).getFirstChild() != null ? codigoconsulta.item(0).getFirstChild().getNodeValue() : "";
				tbL = tbL + L1 + "CODIGO DA CONSULTA:" + codigoDaConsulta + L2;
				tbL = tbL + "</table><br>";
				tbL = tbL + tb + "<tr><td><font class='cabecalho'> Informações Gerais</font></td></tr><table>";
				String pesqcpfcnpj = "<a href='#' onclick=PesqCpfCnpj('"
						+ strCpfCnpjCredito + "')>";
				String strConsultasRealizadas = consultasrealizadas.item(0).getFirstChild() !=null ? consultasrealizadas.item(0).getFirstChild().getNodeValue() : "";
				if (strCpfCnpjCredito.length() == 11) {
	
					tbL = tbL + tb + "<tr>" + col + "Cpf</font></td>" + col + "Nome Completo</font></td>" + col
							+ "Filiação</font></td>" + col + "Identidade</font></td>" + col + "Dt. Nascimento</font></td>"
							+ col + "Signo</font></td>" + col + "Consultas Realizadas</font></td>";
					tbL = tbL + col + "Qtd Restrições Bancárias</font></td>" + col + "Qtd Restricões Lojistas</font></td>"
							+ col + "Qtd. Cheques Pré-Datados</font></td>" + col + "Qtd. Alertas</font></td></tr>";
					tbL = tbL + "<tr>" + col + pesqcpfcnpj + strCpfCnpjCredito
							+ "</a></font></td>" + col + nomecomp + "</font></td>" + col + filtab + "</font></td>" + col
							+ idtab + "</font></td>" + col + nasctab + "</font></td>";
					tbL = tbL + col + sigtab + "</font></td>" + col
							+ strConsultasRealizadas + "</font></td>";
					tbL = tbL + col + qtdbancarias + "</font></td>" + col + qtdlojista + "</font></td>" + col
							+ qtdchequespre + "</font></td>";
					tbL = tbL + col + qtdalerta + "</font></td></tr>";
					tbL = tbL + "</table><br>";
	
				} else {
	
					tbL = tbL + tb + "<tr>" + col + "Cnpj</font></td>" + col + "Nome da Empresa</font></td>" + col
							+ "Inf. Adicional</font></td>" + col + "Outros Documentos</font></td>" + col
							+ "Dt. Fundacao</font></td>" + col + "Consultas Realizadas</font></td>";
					tbL = tbL + col + "Qtd Restrições Bancárias</font></td>" + col + "Qtd Restricões Lojistas</font></td>"
							+ col + "Qtd. Cheques Pré-Datados</font></td>" + col + "Qtd. Alertas</font></td></tr>";
					tbL = tbL + "<tr>" + col + pesqcpfcnpj + strCpfCnpjCredito
							+ "</a></font></td>" + col + nomecompE + "</font></td>" + col + filtab + "</font></td>" + col
							+ "--</font></td>" + col + nasctab + "</font></td>";
					tbL = tbL + col + strConsultasRealizadas + "</font></td>";
					tbL = tbL + col + qtdbancarias + "</font></td>" + col + qtdlojista + "</font></td>" + col
							+ qtdchequespre + "</font></td>";
					tbL = tbL + col + qtdalerta + "</font></td></tr>";
					tbL = tbL + "</table><br>";
	
					// Registro da Empresa
					if (empresacredito.getLength() > 0) {
	
						tbL = tbL + "<br>";
						tbL = tbL + tb
								+ "<tr><td><font class='cabecalho'>Informações do Registro da Empresa</font></td></tr></table>";
						tbL = tbL + tb;
						tbL = tbL + "<tr>" + col + "Dt. Inicio</font></td>" + col + "Razão Social</font></td>" + col
								+ "Endereço</font></td>" + col + "Bairro</font></td>" + col + "Cep</font></td>" + col
								+ "Cidade</font></td>" + col + "Uf</font></td></tr>";
						tbL = tbL + "<tr>" + col + dtiregempT.substring(6, 8) + "/" + dtiregempT.substring(4, 6) + "/"
								+ dtiregempT.substring(0, 4) + "</font></td>" + col + nomecompE + "</font></td>" + col
								+ endempT + "</font></td>" + col + bairroempT + "</font></td>" + col + "Cep</font></td>"
								+ col + cidempT + "</font></td>" + col + ufempT + "</font></td></tr>";
						tbL = tbL + "</table><br>";
	
					}
					// Perfil Economico
					if (rpj == 1) {
	
						tbL = tbL + "<br>";
						tbL = tbL + tb + "<tr><td><font class='cabecalho'>Perfil Econômico</font></td></tr></table>";
						tbL = tbL + tb;
						tbL = tbL + "<tr>" + col + "Porte</font></td>" + col + "Tipo Imposto</font></td>" + col
								+ "Total Funcionarios</font></td>" + col + "Func. com Renda Maior que 5 mínimos</font></td>"
								+ col + "Func. com Renda Menor que 5 mínimos</font></td></tr>";
						tbL = tbL + "<tr>" + col + porteT + "</font></td>" + col + impostoT + "</font></td>" + col
								+ totalfuncionariosT + "</font></td>" + col + funcmaior5T + "</font></td>" + col
								+ funcmenor5T + "</font></td></tr>";
						tbL = tbL + "</table><br>";
	
					}
	
					// Socios
	
					if (socios.getLength() > 0) {
	
						tbL = tbL + "<br>";
						tbL = tbL + tb
								+ "<tr><td><font class='cabecalho'>Informações Sobre os Sócios</font></td></tr></table>";
						tbL = tbL + tb;
						tbL = tbL + "<tr>" + col + "Dt. Inicio</font></td>" + col + "Nome</font></td>" + col
								+ "Endereço</font></td>" + col + "Cidade</font></td>" + col + "Bairro</font></td>" + col
								+ "Cep</font></td>" + col + "Uf</font></td></tr>";
	
						for (int Y = 0; Y < socios.getLength(); Y++) {
	
							/*
							 * dtiniciosocio = socios.item(Y).getAttributes().getNamedItem(
							 * "dtiniciosocio"); nomesocio = socios.item(Y).getAttributes().getNamedItem(
							 * "nomesocio"); endsocio = socios.item(Y).getAttributes().getNamedItem(
							 * "enderecosocio"); cepsocio = socios.item(Y).getAttributes().getNamedItem(
							 * "cepsocio"); bairrosocio = socios.item(Y).getAttributes().getNamedItem(
							 * "bairrosocio"); cidadesocio = socios.item(Y).getAttributes().getNamedItem(
							 * "cidadesocio"); ufsocio =
							 * socios.item(Y).getAttributes().getNamedItem("ufsocio" ); dtiniciosocioT =
							 * dtiniciosocio.getNodeValue(); nomesocioT = nomesocio.getNodeValue();
							 */
	
							dtiniciosocioT = socios.item(Y).getChildNodes().item(2).getTextContent();
							nomesocioT = socios.item(Y).getChildNodes().item(1).getTextContent();
	
							if (socios.item(Y).getChildNodes().item(3).getTextContent() != null) {
	
								endsocioT = socios.item(Y).getChildNodes().item(3).getTextContent();
	
							} else {
	
								endsocioT = "--";
	
							}
	
							if (socios.item(Y).getChildNodes().item(6).getTextContent() != null) {
	
								cepsocioT = socios.item(Y).getChildNodes().item(6).getTextContent();
	
							} else {
	
								cepsocioT = "--";
	
							}
	
							if (socios.item(Y).getChildNodes().item(7).getTextContent() != null) {
	
								bairrosocioT = socios.item(Y).getChildNodes().item(7).getTextContent();
	
							} else {
	
								bairrosocioT = "--";
	
							}
	
							if (socios.item(Y).getChildNodes().item(8).getTextContent() != null) {
	
								cidadesocioT = socios.item(Y).getChildNodes().item(8).getTextContent();
	
							} else {
	
								cidadesocioT = "--";
	
							}
	
							if (socios.item(Y).getChildNodes().item(9).getTextContent() != null) {
	
								ufsocioT = socios.item(Y).getChildNodes().item(9).getTextContent();
	
							} else {
	
								ufsocioT = "--";
	
							}
	
							tbL = tbL + "<tr>" + col + "</font></td>" + col + nomesocioT + "</font></td>" + col + endsocioT
									+ "</font></td>" + col + cidadesocioT + "</font></td>" + col + bairrosocioT
									+ "</font></td>" + col + cepsocioT + "</font></td>" + col + ufsocioT
									+ "</font></td></tr>";
	
						}
	
						tbL = tbL + "</table><br>";
					}
	
				}
				if (telefonesProprietario != null && telefonesProprietario.size() > 0) {
					tbL = tbL + tb + "<tr><td><font class='cabecalho'>Telefones do Proprietário</font></td></tr>";
					tbL = tbL + tb + "<tr>" + col + "Telefone</font></td>" + col + "Endereço</font></td>";
					for (int i = 0; i < telefonesProprietario.size(); i++) {
						tbL = tbL + "<tr>" + col + telefonesProprietario.get(i) + "</font></td>" + col
								+ logradourosProprietario.get(i) + ", " + bairrosProprietario.get(i) + ", "
								+ cidadesProprietario.get(i) + " - " + ufsProprietario.get(i) + " "
								+ cepsProprietario.get(i) + "</font></td>";
					}
					tbL += "</table><br>";
				}
				if (telefonesReferencia != null && telefonesReferencia.size() > 0) {
					tbL = tbL + tb + "<tr><td><font class='cabecalho'>Telefones de Referência</font></td></tr>";
					tbL = tbL + tb + "<tr>" + col + "Proprietário</font></td>" + col + "Telefone</font></td>" + col
							+ "Endereço</font></td>";
					for (int i = 0; i < telefonesReferencia.size(); i++) {
						tbL = tbL + "<tr>" + col + proprietariosReferencia.get(i) + "</font></td>" + col
								+ telefonesReferencia.get(i) + "</font></td>" + col + logradourosReferencia.get(i) + ", "
								+ bairrosReferencia.get(i) + ", " + cidadesReferencia.get(i) + " - " + ufsReferencia.get(i)
								+ " " + cepsReferencia.get(i) + "</font></td>";
					}
					tbL += "</table><br>";
				}
				// Tratamento de Bancos
				if (Integer.parseInt(qtdbancos) > 0) {
					tbL = tbL + "<br>";
	
					conta = consultabancos.item(0).getChildNodes();
					tbL = tbL + tb + "<tr><td><font class='cabecalho'>Contas em Bancos</font></td></tr><table>";
					tbL = tbL + tb + "<tr>";
	
					for (int X = 0; X < conta.getLength(); X++) {
	
						if (conta.item(X).getNodeName().equals("conta")) {
	
							tbL = tbL + col + conta.item(X).getTextContent() + "</font></td>";
	
						}
	
					}
					tbL = tbL + "</tr></table>";
				}
	
				// Tratamento de Alertas
	
				if (Integer.parseInt(qtdalerta) > 0) {
					tbL = tbL + "<br>";
					NodeList alertascpf = alertas.item(0).getChildNodes();
					tbL = tbL + tb + "<tr><td><font class='cabecalho'>Alertas</font></td></tr></table>";
					tbL = tbL + tb;
					tbL = tbL + "<tr>" + col + "Dt. Alerta</font></td>" + col + "Informação do Alerta</font></td></tr>";
	
					for (int Z = 1; Z < alertascpf.getLength(); Z++) {
						/*
						 * malerta = alertaaux.getAttributes().getNamedItem("mensagem"); mdt =
						 * alertaaux.getAttributes().getNamedItem("data");
						 */
						msgalerta = alertascpf.item(Z).getChildNodes().item(0).getTextContent();
						dtalerta = alertascpf.item(Z).getChildNodes().item(1).getTextContent();
	
						tbL = tbL + "<tr>" + col + dtalerta + "</font></td>" + col + msgalerta + "</font></td></tr>";
					}
					tbL = tbL + "</table>";
				} else {
	
					tbL = tbL + "<br>";
					tbL = tbL + tb + "<tr><td><font class='cabecalho'>Alertas</font></td></tr></table>";
					tbL = tbL + tb;
					tbL = tbL + "<tr>" + col + "Dt. Alerta</font></td>" + col + "Informação do Alerta</font></td></tr>";
					tbL = tbL + "<tr>" + col + " -- </font></td>" + col
							+ "NÃO EXISTEM ALERTAS PARA ESSE CPF/CNPJ</font></td></tr>";
					tbL = tbL + "</table>";
				}
	
				// Tratamento das Restricoes Bancarias
				if (Integer.parseInt(qtdbancarias) > 0) {
					tbL = tbL + "<br>";
					NodeList restricaoaux = restricoesbancarias.item(0).getChildNodes();
					tbL = tbL + tb + "<tr><td><font class='cabecalho'>Restrições Bancárias</font></td></tr></table>";
					tbL = tbL + tb;
					tbL = tbL + "<tr>" + col + "Dt.Restrição</font></td>" + col + "Banco</font></td>" + col
							+ "Agencia</font></td>" + col + "Quantidade</font></td></tr>";
					for (int G = 1; G < restricaoaux.getLength(); G++) {
						dtrT = restricaoaux.item(G).getChildNodes().item(0).getTextContent();
						bcoT = restricaoaux.item(G).getChildNodes().item(1).getTextContent();
						ageT = restricaoaux.item(G).getChildNodes().item(2).getTextContent();
						qtdT = restricaoaux.item(G).getChildNodes().item(3).getTextContent();
						tbL = tbL + "<tr>" + col + dtrT + "</font></td>" + col + bcoT + "</font></td>" + col + ageT
								+ "</font></td>" + col + qtdT + "</font></td></tr>";
					}
					tbL = tbL + "</table>";
				}
	
				// Tratamento Restricoes Lojistas
	
				if (Integer.parseInt(qtdlojista) > 0) {
					tbL = tbL + "<br>";
					NodeList ljauxs = restricoeslojistas.item(0).getChildNodes();
					tbL = tbL + tb + "<tr><td><font class='cabecalho'>Restrições Lojistas</font></td></tr></table>";
					tbL = tbL + tb;
					tbL = tbL + "<tr>" + col + "Data</font></td>" + col + "Credor</font></td>" + col
							+ "Tipo Doc.</font></td>" + col + "Valor</font></td></tr>";
	
					for (int L = 1; L < ljauxs.getLength(); L++) {
	
						credorT = ljauxs.item(L).getChildNodes().item(0).getTextContent();
						tipodocT = ljauxs.item(L).getChildNodes().item(1).getTextContent();
						valorT = ljauxs.item(L).getChildNodes().item(2).getTextContent();
						dtljT = ljauxs.item(L).getChildNodes().item(3).getTextContent();
	
						tbL = tbL + "<tr>" + col + dtljT + "</font></td>" + col + credorT + "</font></td>" + col + tipodocT
								+ "</font></td>" + col + valorT + "</font></td></tr>";
	
					}
					tbL = tbL + "</table>";
				} else {
	
					tbL = tbL + "<br>";
					tbL = tbL + tb + "<tr><td><font class='cabecalho'>Restrições Lojistas</font></td></tr></table>";
					tbL = tbL + tb;
					tbL = tbL + "<tr>" + col + "Data</font></td>" + col + "Credor</font></td>" + col
							+ "Tipo Doc.</font></td>" + col + "Valor</font></td></tr>";
					tbL = tbL + "<tr>" + col + "--</font></td>" + col
							+ "NÃO EXISTEM RESTRIÇÕES LOJISTAS PARA ESSE CPF/CNPJ</font></td>" + col + "--</font></td>"
							+ col + "--</font></td></tr>";
					tbL = tbL + "</table>";
				}
	
				// Cheques-Pre Datados
				if (Integer.parseInt(qtdchequespre) > 0) {
					tbL = tbL + "<br>";
					NodeList cheques = chequespredatados.item(0).getChildNodes();
	
					tbL = tbL + tb + "<tr><td><font class='cabecalho'>Cheques Pré-Datados</font></td></tr></table>";
					tbL = tbL + tb;
					tbL = tbL + "<tr>" + col + "Emissao</font></td>" + col + "Valor</font></td>" + col + "Banco</font></td>"
							+ col + "Num. do Cheque</font></td>" + col + "Vencimento</font></td>" + col
							+ "Cliente</font></td></tr>";
	
					for (int XX = 1; XX < cheques.getLength(); XX++) {
	
						/*
						 * emissao = cqaux.getAttributes().getNamedItem("emissao"); bcocq =
						 * cqaux.getAttributes().getNamedItem("banco"); ncheq =
						 * cqaux.getAttributes().getNamedItem("numerocheque"); vcocq =
						 * cqaux.getAttributes().getNamedItem("vencimento"); valcli =
						 * cqaux.getAttributes().getNamedItem("cliente"); valcheque =
						 * cqaux.getAttributes().getNamedItem("valor");
						 */
						emissaoT = cheques.item(XX).getChildNodes().item(0).getTextContent();
						bcocqT = cheques.item(XX).getChildNodes().item(1).getTextContent();
						ncheqT = cheques.item(XX).getChildNodes().item(2).getTextContent();
						vcocqT = cheques.item(XX).getChildNodes().item(3).getTextContent();
						valcliT = cheques.item(XX).getChildNodes().item(4).getTextContent();
						valchequeT = cheques.item(XX).getChildNodes().item(5).getTextContent();
	
						tbL = tbL + "<tr>" + col + emissaoT + "</font></td>" + col + valchequeT + "</font></td>" + col
								+ bcocqT + "</font></td>" + col + ncheqT + "</font></td>" + col + vcocqT + "</font></td>"
								+ col + valcliT + "</font></td></tr>";
	
					}
					tbL = tbL + "</table>";
				} else {
	
					tbL = tbL + "<br>";
					tbL = tbL + tb + "<tr><td><font class='cabecalho'>Cheques Pre-Datados</font></td></tr></table>";
					tbL = tbL + tb;
					tbL = tbL + "<tr>" + col + "Data</font></td>" + col + "Credor</font></td>" + col
							+ "Tipo Doc.</font></td>" + col + "Valor</font></td></tr>";
					tbL = tbL + "<tr>" + col + "--</font></td>" + col
							+ "NÃO EXISTEM CHEQUES PRÉ-DATADOS PARA ESSE CPF/CNPJ</font></td>" + col + "--</font></td>"
							+ col + "--</font></td></tr>";
					tbL = tbL + "</table>";
				}
	
				// Renda PF
				if (rpf == 1) {
					tbL = tbL + "<br>";
					tbL = tbL + tb
							+ "<tr><td><font class='cabecalho'>Renda Estimada Por Empresas Trabalhadas</font></td></tr></table>";
					tbL = tbL + tb;
					tbL = tbL + "<tr>" + col + "Renda</font></td>" + col + "Empresa</font></td></tr>";
	
					for (int XY = 0; XY < rendapf.getLength(); XY++) {
						/*
						 * valrenda = rendapf.item(XY).getAttributes().getNamedItem( "valorrenda");
						 * empresapag = rendapf.item(XY).getAttributes().getNamedItem(
						 * "empresapagadora");
						 */
						valrendaT = rendapf.item(XY).getChildNodes().item(1).getTextContent();
						empresapagT = rendapf.item(XY).getChildNodes().item(2).getTextContent();
						tbL = tbL + "<tr>" + col + valrendaT + "</font></td>" + col + empresapagT + "</font></td></tr>";
	
					}
					tbL = tbL + "</table>";
				}
	
				// Ultimas Consultas
	
				tbL = tbL + "<br>";
				tbL = tbL + tb + "<tr><td><font class='cabecalho'>Ultimas Consultas</font></td></tr></table>";
				tbL = tbL + tb;
				tbL = tbL + "<tr>" + col + "Dt.Consulta/Hora</font></td>" + col + "Cliente Credilink</font></td></tr>";
				NodeList registropaux;
				Node registropaux1;
				registropassagem = passagem.item(0).getChildNodes();


				for (int ZY = 0; ZY < registropassagem.getLength(); ZY++) {
	
					registropaux = registropassagem.item(ZY).getChildNodes();
					registropaux1 = registropaux.item(0).getFirstChild() != null ? registropaux.item(0).getFirstChild() : null;
					clientecredT = registropaux1 != null ? registropaux1.getNodeValue() : "";
					registropaux1 = registropaux.item(1).getFirstChild() != null ? registropaux.item(1).getFirstChild() : null;
					datahoraT = registropaux1 != null ? registropaux1.getNodeValue() : "";
	
					if(!ultimaConsultaMaiorQueCincoAnos(datahoraT))
					tbL = tbL + "<tr>" + col + datahoraT + "</font></td>" + col + clientecredT + "</font></td></tr>";
	
				}
	
				tbL = tbL + "</table>";
			
			
			} //menorDeIdade

			this.setHtmlrespcredito(tbL);

			mx.setResposta_historico_credito(true);
			mx.setResposta_consulta(false);
			mx.setResposta_conArmazenada(false);
			mx.setResposta_endereco(false);
			mx.setResposta_nome(false);
			mx.setResposta_razao(false);
			mx.setResposta_cep(false);
			mx.setResposta_veiculo(false);
			mx.setResposta_obitoNacional(false);
			this.setForm_active_cpfcnpj("form");
			this.setForm_active_telefone("form");
			this.setForm_active_operadora("form");
			this.setForm_active_cep("form");
			this.setForm_active_endereco("form");
			this.setForm_active_historico_credito("form active");
			this.setForm_active_nome("form");
			this.setForm_active_razao_social("form");
			this.setForm_active_obitoNacional("form");
			this.setForm_active_veiculos("form");

			/*
			 * monta o xml nas variaveis para retornar para a pagina principal
			 */

			return true;

		} catch (Exception e) {
		logger.error("erro na classe Resposta metodo: pesquisaConsultaCredito "+ e.getMessage());
			return false;
		}

	}

	private boolean infoRestricaoOk(NodeList infoRestricao, String status) {
				if(infoRestricao == null)
					return false;
				if(infoRestricao.item(0) == null)
					return false;
				if(infoRestricao.item(0).getFirstChild() == null)
					return false;
				if(infoRestricao.item(0).getFirstChild().getNodeValue() == null)
					return false;
				if(infoRestricao.item(0).getFirstChild().getNodeValue().equals(status))
					return true;
				return false;

	}

	private boolean ultimaConsultaMaiorQueCincoAnos(String datahoraT) {

		boolean resultado = false;

		if(datahoraT.equals(""))
			return false;

		if(datahoraT.length() == 19)
			datahoraT = removeExcessoDeStringDaData(datahoraT);

		try{
			
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy H:mm:ss");
			
			LocalDateTime dataDoRetornoDaPesquisa = LocalDateTime.parse(datahoraT, formatter);
			LocalDateTime  dataAtual = LocalDateTime.now();
			LocalDateTime cincoAnosAtras = dataAtual.plus(-5, ChronoUnit.YEARS );

			if(dataDoRetornoDaPesquisa.isBefore(cincoAnosAtras))
				resultado =  true;


		}catch (Exception e){
			logger.error("Erro na classe Resposta na funcao ultimaConsultaMaiorQueCincoAnos "+ e.getMessage());
		}

return resultado;
	}

	private String removeExcessoDeStringDaData(String datahoraT) {

		if(datahoraT.substring(11, 12).equals(" "))
			return datahoraT.replaceFirst(" ", "");
		return datahoraT;
	}

	public String proconSP(String telefone, Connection comm) {

		String bloqueio = "";
		String SQL = null;
		String TIPO = "Não possui bloqueio no Procon";
		String DTLIBERADO = null;
		String UFPROCON = "";
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			SQL = "SELECT 'VarBind' as TESTE, UF_PROCON,TO_CHAR(DT_CADASTRO,'DD/MM/YYYY') AS DT_CADASTRO,TO_CHAR(DT_LIBERADO,'DD/MM/YYYY') AS DT_LIBERADO,ID_TIPO FROM TB_BLOQ_PROCON WHERE TELEFONE = ? AND ID_BLOQUEIO = ( SELECT MAX(ID_BLOQUEIO) FROM TB_BLOQ_PROCON   WHERE TELEFONE = ? )";
//          java.sql.Statement stmtN = comm.createStatement();
//          ResultSet rs = stmtN.executeQuery(SQL);
			// Rodrigo Almeida - 27/01/2020 - Transformando em variável Bind
			comm = this.getConnection();
			// System.out.println("SQL: " + SQL + " " + telefone);
			stmt = comm.prepareStatement(SQL);
			stmt.setString(1, telefone);
			stmt.setString(2, telefone);
			rs = stmt.executeQuery();
			if (rs != null && rs.next()) {
				// System.out.println("bloqueio pROCON");
				try {
					TIPO = rs.getString("ID_TIPO");
					DTLIBERADO = rs.getString("DT_LIBERADO");
					UFPROCON = rs.getString("UF_PROCON");
					if (TIPO.equals("2")) {
						TIPO = "Não possui bloqueio no Procon";
					}
				} catch (Exception e) {
					System.out.println("Error: " + e.getMessage() + " Resposta - proconSP " + SQL
							+ " - parâmtro Telefone: " + telefone);
					TIPO = "Não possui bloqueio no Procon";
				}
				if (!TIPO.equals("Não possui bloqueio no Procon")) {
					TIPO = "Possui bloqueio no Procon " + UFPROCON + " ( " + DTLIBERADO + " )";
				}
			}
			rs.close();
//            stmtN.close();
			stmt.close();
			return TIPO;
		} catch (SQLException e) {
			logger.error("Erro no metodo proconSP da classe Resposta: " + e.getMessage());
			return "NÃO POSSUI BLOQUEIO NO PROCON";
		}
	}

	private static String ccredito(java.lang.String usuario, java.lang.String password, java.lang.String cpfcnpj)
			throws RemoteException, ServiceException {
		Credito creditoService = new CreditoServiceLocator().getCreditoPort();
		return creditoService.ccredito(usuario, password, cpfcnpj);
	}

	public void orderTelefonesByTelefone(String numTelefoneToOrder) {
		if (!estaVazioOuNulo(numTelefoneToOrder)) {
			Telefone telefoneToOrder = null;
			for (Telefone tel : this.telefone) {
				if ((tel.getNumeroTelefone()).equals(numTelefoneToOrder)) {
					telefoneToOrder = tel;
					break;
				}
			}
			if (telefoneToOrder != null) {
				telefone.remove(telefoneToOrder);
				telefone.add(0, telefoneToOrder);
				int idTelefone = 1;
				for (Telefone tel : this.telefone) {
					tel.setId(idTelefone);
					tel.setEoprimeiro(false);
					idTelefone++;
				}

				this.telefone.get(0).setEoprimeiro(true);
			}
		}
	}

	private Integer getNextIdOnTelefones() {
		if (telefone.isEmpty()) {
			return 1;
		} else {
			return telefone.get(telefone.size() - 1).getId() + 1;
		}
	}

	private Boolean estaVazioOuNulo(String string) {
		return (string == null) || (string.equals(""));
	}

	private StringBuilder montaConsultaNome(LoginMBean mx, Integer paginaInicial, Integer paginaFinal) {
		String nome = mx.getPessoaSite().getNome();
		nome = nome.replace("%", "");
		nome = nome.replace("'", "");
		nome = nome.replace("*", "");
		nome = nome.replace("-", "");
		StringBuilder sql = new StringBuilder();

		sql.append("SELECT * FROM ( ");
		sql.append(" SELECT PAGINA.*,ROW_NUMBER() OVER (ORDER BY CPFCNPJ) AS PAGINA_RN FROM ( ");
		// Prepara a query para envio ao Banco de Dados
		sql.append(" SELECT ");
		sql.append(
				" (SELECT TO_CHAR(STATUS) FROM TELEFONES_STATUS WHERE TELEFONE = T.TELEFONE AND ROWNUM <= 1) AS STATUS_LINHA,");
		sql.append(
				" (CASE WHEN (SELECT FLAG FROM TBL_WHATSAPP T2 WHERE T2.TELEFONE = T.TELEFONE AND ROWNUM <= 1) = 1 THEN 1 ELSE 0 END) AS WHATSAPP, ");
		sql.append(" (SELECT IS_DATE_CONFIRME(DT_OBITO) FROM OBITO WHERE NU_CPF = CPFCGC AND ROWNUM<=1 ) AS OBITO,");
		sql.append(" (SELECT DT_ABERTURA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS DT_ABERTURA,");
		sql.append(" (SELECT SITUACAO FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS SITUACAO,");
		sql.append(" (SELECT FANTASIA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS FANTASIA,");
		sql.append(" (SELECT DESC_NATUREZA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS NATUREZA,");
		sql.append(
				" '' AS NOME,T.PROPRIETARIO AS PROPRIETARIO,T.ATUAL,T.TELEFONE,T.ENDERECO AS ENDERECO,TO_CHAR(T.NUMERO) AS NUMERO,T.COMPLEMENTO,T.BAIRRO,T.CEP,T.CIDADE,T.UF,T.CPFCGC AS CPFCNPJ,");
		sql.append(
				"i.CPF_CONJUGE AS CPF_CONJUGE,PEGA_NOME_INFO_COMP(I.CPF_CONJUGE) AS NOME_CONJUGE, I.NOME_MAE AS MAE,I.NOME_PAI AS PAI,TO_CHAR(I.DATA_NASC,'DD/MM/YYYY') AS NASC,' ' AS TITULO,T.OPERADORA,TO_CHAR(T.DT_INSTALACAO,'YYYY-MM-DD') AS DT_INSTALACAO, I.SIGNO AS SIGNO");
		sql.append(" FROM TELEFONES T, FINAN.CRED_MEGA_CEP M, INFO_COMPLEMENTARES I WHERE T.CEP=M.CEP(+) ");
		sql.append(" AND T.CPFCGC=I.CPFCNPJ(+) ");
		// sql.append(" AND I.CPFCNPJ=T.CPFCGC(+) ");

		if (!estaVazioOuNulo(mx.getPessoaSite().getEndereco().getUf().toString())) {

			sql.append("AND T.UF = '" + mx.getPessoaSite().getEndereco().getUf() + "' ");

		}

		sql.append(" AND T.PROPRIETARIO LIKE '" + nome + "%'");
		// sql.append(" AND I.NOME LIKE '" + nome + "%'");

		// Se o bairro foi colocado, poe no SQL para ficar mais rápido
		if (!estaVazioOuNulo(mx.getPessoaSite().getEndereco().getBairro())) {

			sql.append(" AND T.BAIRRO = '" + mx.getPessoaSite().getEndereco().getBairro() + "' ");

		}

		// Se a cidade foi colocada, poe no SQL para ficar mais rapido

		if (!estaVazioOuNulo(mx.getPessoaSite().getEndereco().getCidade())) {

			sql.append(" AND T.CIDADE = '" + mx.getPessoaSite().getEndereco().getCidade() + "' ");

		}

		// Fechamento do SQL
		sql.append("AND NOT EXISTS (SELECT * FROM PROTECAO_CPFCNPJ WHERE USUARIO = '" + mx.getUsuario().getLogin()
				+ "' AND CPFCGC = T.CPFCGC ) ");
		// sql.append(" ORDER BY ATUAL DESC");
		sql.append(" AND ROWNUM <= " + br.com.confirmeonline.util.SQLConstantes.QTD_MAX_PESQUISA + " ");

		sql.append(" UNION ALL ");

		sql.append(" SELECT '' AS STATUS_LINHA, ");
		sql.append(" '' AS WHATSAPP, ");
		sql.append(
				" (SELECT IS_DATE_CONFIRME(DT_OBITO) FROM OBITO WHERE NU_CPF = I.CPFCNPJ AND ROWNUM <= 1) AS OBITO, ");
		sql.append(" (SELECT DT_ABERTURA FROM QSA_EMPRESAS WHERE CNPJ = I.CPFCNPJ AND ROWNUM <= 1) AS DT_ABERTURA, ");
		sql.append(" (SELECT SITUACAO FROM QSA_EMPRESAS WHERE CNPJ = I.CPFCNPJ AND ROWNUM <= 1) AS SITUACAO, ");
		sql.append(" (SELECT FANTASIA FROM QSA_EMPRESAS WHERE CNPJ = I.CPFCNPJ AND ROWNUM <= 1)AS FANTASIA, ");
		sql.append(" (SELECT DESC_NATUREZA FROM QSA_EMPRESAS WHERE CNPJ = I.CPFCNPJ AND ROWNUM <= 1)AS NATUREZA, ");
		sql.append(
				" I.NOME,'' AS PROPRIETARIO,'' AS ATUAL,'' AS TELEFONE,I.ENDERECO AS ENDERECO,TO_CHAR(I.NUMERO) AS NUMERO,I.COMPLEMENTO,I.BAIRRO,I.CEP,I.CIDADE, ");
		sql.append(
				" i.CPF_CONJUGE  AS CPF_CONJUGE,PEGA_NOME_INFO_COMP(I.CPF_CONJUGE) AS NOME_CONJUGE,I.UF,I.CPFCNPJ AS CPFCNPJ,I.NOME_MAE AS MAE, ");
		sql.append(
				" TO_CHAR (I.DATA_NASC, 'DD/MM/YYYY') AS NASC,'' AS TITULO,'' AS OPERADORA,'' DT_INSTALACAO, I.SIGNO AS SIGNO ");
		sql.append(" FROM INFO_COMPLEMENTARES I ");
		sql.append(" WHERE 1=1 ");

		if (!estaVazioOuNulo(mx.getPessoaSite().getEndereco().getUf().toString())) {

			sql.append("AND I.UF = '" + mx.getPessoaSite().getEndereco().getUf() + "' ");

		}
		// Modificado Sebastiao MArcio para dar performane em 10/03/2014
		// sql.append(" AND I.NOME LIKE '" + nome + "%'");
		sql.append(" AND I.NOME LIKE '" + nome + "%' ");
		// sql.append(" AND T.NOME LIKE '" + nome + "%'");

		// Se o bairro foi colocado, poe no SQL para ficar mais rápido
		if (!estaVazioOuNulo(mx.getPessoaSite().getEndereco().getBairro())) {

			sql.append(" AND I.BAIRRO = '" + mx.getPessoaSite().getEndereco().getBairro() + "' ");

		}

		// Se a cidade foi colocada, poe no SQL para ficar mais rapido

		if (!estaVazioOuNulo(mx.getPessoaSite().getEndereco().getCidade())) {

			sql.append(" AND I.CIDADE = '" + mx.getPessoaSite().getEndereco().getCidade() + "' ");

		}

		// Fechamento do SQL
		sql.append("AND NOT EXISTS (SELECT * FROM PROTECAO_CPFCNPJ WHERE USUARIO = '" + mx.getUsuario().getLogin()
				+ "' AND CPFCGC = I.CPFCNPJ ) ");
		// sql.append(" ORDER BY ATUAL DESC");

		sql.append(" AND ROWNUM <= " + br.com.confirmeonline.util.SQLConstantes.QTD_MAX_PESQUISA + " ");
		sql.append(" UNION ALL ");
		sql.append(" SELECT '' AS status_linha, ");
		sql.append(" '' AS WHATSAPP, ");
		sql.append(" '' AS obito, ");
		sql.append(" q.dt_abertura,q.desc_natureza AS natureza,q.fantasia,q.situacao, ");
		sql.append(" to_char(q.razao_social) as nome, '' AS proprietario, '' AS atual, ");
		sql.append(" '' AS telefone, to_char(q.endereco) AS endereco, ");
		sql.append(" TO_CHAR (q.numero) AS numero, to_char(q.complemento), to_char(q.bairro), ");
		sql.append(" q.cep, to_char(q.cidade), to_char(q.uf), to_char(q.cnpj) AS cpfcnpj, ");
		sql.append(" '' AS mae, ");
		sql.append(" '' AS nasc, ");
		sql.append(" '' AS titulo, '' AS operadora, '' dt_instalacao, ");
		sql.append(" '' AS signo ");
		sql.append(" FROM qsa_empresas q ");
		sql.append(" WHERE 1 = 1 ");
		if (!estaVazioOuNulo(mx.getPessoaSite().getEndereco().getUf().toString())) {
			sql.append("AND Q.UF = '" + mx.getPessoaSite().getEndereco().getUf() + "' ");
		}

		sql.append(" AND Q.RAZAO_SOCIAL LIKE '" + nome + "%' ");

		if (!estaVazioOuNulo(mx.getPessoaSite().getEndereco().getBairro())) {
			sql.append(" AND Q.BAIRRO = '" + mx.getPessoaSite().getEndereco().getBairro() + "' ");
		}

		// Se a cidade foi colocada, poe no SQL para ficar mais rapido

		if (!estaVazioOuNulo(mx.getPessoaSite().getEndereco().getCidade())) {
			sql.append(" AND Q.CIDADE = '" + mx.getPessoaSite().getEndereco().getCidade() + "' ");
		}

		sql.append(" AND NOT EXISTS ( SELECT * FROM protecao_cpfcnpj WHERE usuario = '" + mx.getUsuario().getLogin()
				+ "' AND cpfcgc = q.cnpj) ");
		sql.append(" AND ROWNUM <= " + br.com.confirmeonline.util.SQLConstantes.QTD_MAX_PESQUISA + " ");

		sql.append(" ) PAGINA  ORDER BY CPFCNPJ ");
		// sql.append(" ) WHERE ( PAGINA_RN >= " + paginaInicial + " AND
		// PAGINA_RN <= "+ paginaFinal +" ) ORDER BY TO_NUMBER(ATUAL) DESC ");
		sql.append(" ) WHERE  ( PAGINA_RN >=  " + paginaInicial + "  AND  PAGINA_RN <= " + paginaFinal + " ) ");

		return sql;
	}

	private StringBuilder montaConsultaNomeJoinTelefonesAndInfoComplementares(LoginMBean mx, String sqlEntrada) {
		String nome = mx.getPessoaSite().getNome();
		nome = nome.replace("%", "");
		nome = nome.replace("'", "");
		nome = nome.replace("*", "");
		nome = nome.replace("-", "");
		StringBuilder sql = new StringBuilder(sqlEntrada);
		// Prepara a query para envio ao Banco de Dados
		sql.append(" SELECT ");
		sql.append(
				" (SELECT STATUS FROM TELEFONES_STATUS WHERE TELEFONE = T.TELEFONE AND ROWNUM <= 1) AS STATUS_LINHA,");
		sql.append(
				" (CASE WHEN (SELECT FLAG FROM TBL_WHATSAPP T2 WHERE T2.TELEFONE = T.TELEFONE AND ROWNUM <= 1) = 1 THEN 1 ELSE 0 END) AS WHATSAPP, ");
		sql.append(" (SELECT IS_DATE_CONFIRME(DT_OBITO) FROM OBITO WHERE NU_CPF = CPFCGC AND ROWNUM<=1 ) AS OBITO,");
		sql.append(" (SELECT DT_ABERTURA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS DT_ABERTURA,");
		sql.append(" (SELECT SITUACAO FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS SITUACAO,");
		sql.append(" (SELECT FANTASIA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS FANTASIA,");
		sql.append(" (SELECT DESC_NATUREZA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS NATUREZA,");
		sql.append(
				" I.NOME,T.PROPRIETARIO AS PROPRIETARIO,T.ATUAL,T.TELEFONE,T.ENDERECO AS ENDERECO,T.NUMERO AS NUMERO,T.COMPLEMENTO,T.BAIRRO,T.CEP,T.CIDADE,T.UF,T.CPFCGC AS CPFCNPJ,");
		sql.append(
				" i.CPF_CONJUGE  AS CPF_CONJUGE,PEGA_NOME_INFO_COMP(I.CPF_CONJUGE) AS NOME_CONJUGE,I.NOME_MAE AS MAE,I.NOME_PAI AS PAI,TO_CHAR(I.DATA_NASC,'DD/MM/YYYY') AS NASC,' ' AS TITULO,T.OPERADORA,TO_CHAR(T.DT_INSTALACAO,'YYYY-MM-DD') AS DT_INSTALACAO, I.SIGNO AS SIGNO");
		sql.append(" FROM TELEFONES T, FINAN.CRED_MEGA_CEP M, INFO_COMPLEMENTARES I WHERE T.CEP=M.CEP(+) ");
		sql.append(" AND T.CPFCGC=I.CPFCNPJ(+) ");
		// sql.append(" AND I.CPFCNPJ=T.CPFCGC(+) ");

		if (!estaVazioOuNulo(mx.getPessoaSite().getEndereco().getUf().toString())) {

			sql.append("AND T.UF = '" + mx.getPessoaSite().getEndereco().getUf() + "' ");

		}

		sql.append(" AND T.PROPRIETARIO LIKE '" + nome + "%'");
		// sql.append(" AND I.NOME LIKE '" + nome + "%'");

		// Se o bairro foi colocado, poe no SQL para ficar mais rápido
		if (!estaVazioOuNulo(mx.getPessoaSite().getEndereco().getBairro())) {

			sql.append(" AND T.BAIRRO = '" + mx.getPessoaSite().getEndereco().getBairro() + "' ");

		}

		// Se a cidade foi colocada, poe no SQL para ficar mais rapido

		if (!estaVazioOuNulo(mx.getPessoaSite().getEndereco().getCidade())) {

			sql.append(" AND T.CIDADE = '" + mx.getPessoaSite().getEndereco().getCidade() + "' ");

		}

		// Fechamento do SQL
		sql.append("AND NOT EXISTS (SELECT * FROM PROTECAO_CPFCNPJ WHERE USUARIO = '" + mx.getUsuario().getLogin()
				+ "' AND CPFCGC = T.CPFCGC ) ");
		// sql.append(" ORDER BY ATUAL DESC");
		return sql;
	}

	private StringBuilder montaConsultaNomeFromInfoComplementares(LoginMBean mx, String sqlEntrada) {
		String nome = mx.getPessoaSite().getNome();
		nome = nome.replace("%", "");
		nome = nome.replace("'", "");
		nome = nome.replace("*", "");
		nome = nome.replace("-", "");
		StringBuilder sql = new StringBuilder(sqlEntrada);
		// Prepara a query para envio ao Banco de Dados
		sql.append(" SELECT '' AS STATUS_LINHA, ");
		sql.append(" '' AS WHATSAPP, ");
		sql.append(
				" (SELECT IS_DATE_CONFIRME(DT_OBITO) FROM OBITO WHERE NU_CPF = I.CPFCNPJ AND ROWNUM <= 1) AS OBITO, ");
		sql.append(" (SELECT DT_ABERTURA FROM QSA_EMPRESAS WHERE CNPJ = I.CPFCNPJ AND ROWNUM <= 1) AS DT_ABERTURA, ");
		sql.append(" (SELECT SITUACAO FROM QSA_EMPRESAS WHERE CNPJ = I.CPFCNPJ AND ROWNUM <= 1) AS SITUACAO, ");
		sql.append(" (SELECT FANTASIA FROM QSA_EMPRESAS WHERE CNPJ = I.CPFCNPJ AND ROWNUM <= 1)AS FANTASIA, ");
		sql.append(" (SELECT DESC_NATUREZA FROM QSA_EMPRESAS WHERE CNPJ = I.CPFCNPJ AND ROWNUM <= 1)AS NATUREZA, ");
		sql.append(
				" I.NOME,'' AS PROPRIETARIO,'' AS ATUAL,'' AS TELEFONE,I.ENDERECO AS ENDERECO,I.NUMERO AS NUMERO,I.COMPLEMENTO,I.BAIRRO,I.CEP,I.CIDADE, ");
		sql.append(
				" i.CPF_CONJUGE AS CPF_CONJUGE,PEGA_NOME_INFO_COMP(I.CPF_CONJUGE) AS NOME_CONJUGE,I.UF,I.CPFCNPJ AS CPFCNPJ,I.NOME_MAE AS MAE,I.NOME_PAI AS PAI, ");
		sql.append(
				" TO_CHAR (I.DATA_NASC, 'DD/MM/YYYY') AS NASC,'' AS TITULO,'' AS OPERADORA,'' DT_INSTALACAO, I.SIGNO AS SIGNO ");
		sql.append(" FROM INFO_COMPLEMENTARES I ");
		sql.append(" WHERE 1=1 ");

		if (!estaVazioOuNulo(mx.getPessoaSite().getEndereco().getUf().toString())) {

			sql.append("AND I.UF = '" + mx.getPessoaSite().getEndereco().getUf() + "' ");

		}
		// Modificado Sebastiao MArcio para dar performane em 10/03/2014
		// sql.append(" AND I.NOME LIKE '" + nome + "%'");
		sql.append(" AND I.NOME LIKE '" + nome + "%'");
		// sql.append(" AND T.NOME LIKE '" + nome + "%'");

		// Se o bairro foi colocado, poe no SQL para ficar mais rápido
		if (!estaVazioOuNulo(mx.getPessoaSite().getEndereco().getBairro())) {

			sql.append(" AND I.BAIRRO = '" + mx.getPessoaSite().getEndereco().getBairro() + "' ");

		}

		// Se a cidade foi colocada, poe no SQL para ficar mais rapido

		if (!estaVazioOuNulo(mx.getPessoaSite().getEndereco().getCidade())) {

			sql.append(" AND I.CIDADE = '" + mx.getPessoaSite().getEndereco().getCidade() + "' ");

		}

		// Fechamento do SQL
		sql.append("AND NOT EXISTS (SELECT * FROM PROTECAO_CPFCNPJ WHERE USUARIO = '" + mx.getUsuario().getLogin()
				+ "' AND CPFCGC = I.CPFCNPJ ) ");
		// sql.append(" ORDER BY ATUAL DESC");
		return sql;
	}

	private SqlToBind montaConsultaFilhosFromInfoComplementares(LoginMBean mx, SqlToBind sqlEntrada) {
		String nome = this.infocomplementares.getNome();
		String digitoRegiao = this.infocomplementares.getCpfcnpj().substring(8, 9);
		String dataNasc = null, uf = null;
		try {
			dataNasc = this.infocomplementares.getDtnasc().split(" - ")[0];

		} catch (Exception e) {
		}
		nome = nome.replace("%", "");
		nome = nome.replace("'", "");
		nome = nome.replace("*", "");
		nome = nome.replace("-", "");
		StringBuilder sql = new StringBuilder(sqlEntrada.getSql());
		// Prepara a query para envio ao Banco de Dados
		sql.append(" SELECT '' AS STATUS_LINHA, ");
		sql.append(" '' AS WHATSAPP, ");
		sql.append(
				" (SELECT IS_DATE_CONFIRME(DT_OBITO) FROM OBITO WHERE NU_CPF = I.CPFCNPJ AND ROWNUM <= 1) AS OBITO, ");
		sql.append(" (SELECT DT_ABERTURA FROM QSA_EMPRESAS WHERE CNPJ = I.CPFCNPJ AND ROWNUM <= 1) AS DT_ABERTURA, ");
		sql.append(" (SELECT SITUACAO FROM QSA_EMPRESAS WHERE CNPJ = I.CPFCNPJ AND ROWNUM <= 1) AS SITUACAO, ");
		sql.append(" (SELECT FANTASIA FROM QSA_EMPRESAS WHERE CNPJ = I.CPFCNPJ AND ROWNUM <= 1)AS FANTASIA, ");
		sql.append(" (SELECT DESC_NATUREZA FROM QSA_EMPRESAS WHERE CNPJ = I.CPFCNPJ AND ROWNUM <= 1)AS NATUREZA, ");
		sql.append(
				" I.NOME,'' AS PROPRIETARIO,'' AS ATUAL,'' AS TELEFONE,I.ENDERECO AS ENDERECO,I.NUMERO AS NUMERO,I.COMPLEMENTO,I.BAIRRO,I.CEP,I.CIDADE, ");
		sql.append(
				" i.CPF_CONJUGE AS CPF_CONJUGE,PEGA_NOME_INFO_COMP(I.CPF_CONJUGE) AS NOME_CONJUGE,I.UF,I.CPFCNPJ AS CPFCNPJ,I.NOME_MAE AS MAE,I.NOME_PAI AS PAI, ");
		sql.append(
				" TO_CHAR (I.DATA_NASC, 'DD/MM/YYYY') AS NASC,'' AS TITULO,'' AS OPERADORA,'' DT_INSTALACAO, I.SIGNO AS SIGNO ");
		sql.append(" FROM FINAN.CRED_MEGA_CEP M,INFO_COMPLEMENTARES I ");
		sql.append(" WHERE     I.CEP = M.CEP(+) ");
		sql.append(" AND I.NOME_MAE = ? ");
		sqlEntrada.addString(nome);
		if (dataNasc != null) {
			sql.append(
					" AND ((I.DATA_NASC IS NULL) OR ((TRUNC((MONTHS_BETWEEN(SYSDATE, TO_DATE(?,'dd/mm/yyyy')))/12)-TRUNC((MONTHS_BETWEEN(SYSDATE, I.DATA_NASC))/12))>=13)) ");
			sqlEntrada.addString(dataNasc);
		}
		// Fechamento do SQL
		sql.append(" AND SUBSTR(I.CPFCNPJ,9,1)= ? ");
		sqlEntrada.addString(digitoRegiao);
		// Fechamento do SQL
		sql.append("AND NOT EXISTS (SELECT * FROM PROTECAO_CPFCNPJ WHERE USUARIO = ? AND CPFCGC = I.CPFCNPJ ) ");
		sqlEntrada.addString(mx.getUsuario().getLogin());
		// sql.append(" ORDER BY ATUAL DESC");

		sqlEntrada.setSql(sql.toString());

		return sqlEntrada;
	}

	private StringBuilder montaConsultaNomeFromTelefones(LoginMBean mx, String sqlEntrada) {
		String nome = mx.getPessoaSite().getNome();
		nome = nome.replace("%", "");
		nome = nome.replace("'", "");
		nome = nome.replace("*", "");
		nome = nome.replace("-", "");
		StringBuilder sql = new StringBuilder(sqlEntrada);
		// Prepara a query para envio ao Banco de Dados
		sql.append(" SELECT ");
		sql.append(
				" (SELECT STATUS FROM TELEFONES_STATUS WHERE TELEFONE = T.TELEFONE AND ROWNUM <= 1) AS STATUS_LINHA,");
		sql.append(
				" (CASE WHEN (SELECT FLAG FROM TBL_WHATSAPP T2 WHERE T2.TELEFONE = T.TELEFONE AND ROWNUM <= 1) = 1 THEN 1 ELSE 0 END) AS WHATSAPP, ");
		sql.append(" (SELECT IS_DATE_CONFIRME(DT_OBITO) FROM OBITO WHERE NU_CPF = CPFCGC AND ROWNUM<=1 ) AS OBITO,");
		sql.append(" (SELECT DT_ABERTURA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS DT_ABERTURA,");
		sql.append(" (SELECT SITUACAO FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS SITUACAO,");
		sql.append(" (SELECT FANTASIA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS FANTASIA,");
		sql.append(" (SELECT DESC_NATUREZA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS NATUREZA,");
		sql.append("  T.PROPRIETARIO as NOME, ");
		sql.append(
				"T.PROPRIETARIO AS PROPRIETARIO,T.ATUAL,T.TELEFONE,T.ENDERECO AS ENDERECO,T.NUMERO AS NUMERO,T.COMPLEMENTO,T.BAIRRO,T.CEP,T.CIDADE,T.UF, ");
		sql.append("T.CPFCGC AS CPFCNPJ,");
		sql.append(" (SELECT I.NOME_MAE FROM INFO_COMPLEMENTARES I WHERE I.CPFCNPJ = T.CPFCGC) AS MAE, ");
		sql.append(
				"TO_CHAR ((SELECT I.DATA_NASC FROM INFO_COMPLEMENTARES I WHERE I.CPFCNPJ = T.CPFCGC), 'DD/MM/YYYY') AS NASC, ");
		sql.append("' ' AS TITULO,T.OPERADORA,TO_CHAR(T.DT_INSTALACAO,'YYYY-MM-DD') AS DT_INSTALACAO, ");
		sql.append(" (SELECT I.SIGNO FROM INFO_COMPLEMENTARES I WHERE I.CPFCNPJ = T.CPFCGC) AS SIGNO ");
		sql.append(" FROM TELEFONES T, FINAN.CRED_MEGA_CEP M WHERE T.CEP=M.CEP(+) ");

		if (!estaVazioOuNulo(mx.getPessoaSite().getEndereco().getUf().toString())) {

			sql.append("AND T.UF = '" + mx.getPessoaSite().getEndereco().getUf() + "' ");

		}

		// sql.append(" AND T.PROPRIETARIO LIKE '" + nome + "%'");
		sql.append(" AND T.PROPRIETARIO LIKE '" + nome + "%'");

		// Se o bairro foi colocado, poe no SQL para ficar mais rápido
		if (!estaVazioOuNulo(mx.getPessoaSite().getEndereco().getBairro())) {

			sql.append(" AND T.BAIRRO = '" + mx.getPessoaSite().getEndereco().getBairro() + "' ");

		}

		// Se a cidade foi colocada, poe no SQL para ficar mais rapido

		if (!estaVazioOuNulo(mx.getPessoaSite().getEndereco().getCidade())) {

			sql.append(" AND T.CIDADE = '" + mx.getPessoaSite().getEndereco().getCidade() + "' ");

		}

		// Fechamento do SQL
		sql.append("AND NOT EXISTS (SELECT * FROM PROTECAO_CPFCNPJ WHERE USUARIO = '" + mx.getUsuario().getLogin()
				+ "' AND CPFCGC = T.CPFCGC ) ");
		// sql.append(" ORDER BY ATUAL DESC");
		return sql;

	}

	private SqlToBind montaFinalDaConsulta(SqlToBind sqlEntrada, Integer paginaInicial, Integer paginaFinal) {
		StringBuilder sql = new StringBuilder(sqlEntrada.getSql());

		sql.append(
				" AND ROWNUM <= 250 ORDER BY CPFCNPJ, TO_NUMBER(ATUAL) DESC  ) PAGINA OFFSET ? - 1    ROWS FETCH NEXT   ? - (? -1)  ROWS ONLY) ");
		// sql.append(" ) WHERE ( PAGINA_RN >= " + paginaInicial + " AND
		// PAGINA_RN <= "+ paginaFinal +" ) ORDER BY TO_NUMBER(ATUAL) DESC ");
		/*
		 * sql.append(" )" + " WHERE  ( PAGINA_RN >=  ?  AND  PAGINA_RN <= ? ) ");
		 */
		// sqlEntrada.addString(String.valueOf(br.com.confirmeonline.util.SQLConstantes.QTD_MAX_PESQUISA));
		sqlEntrada.addString(String.valueOf(paginaInicial));
		sqlEntrada.addString(String.valueOf(paginaFinal));
		sqlEntrada.addString(String.valueOf(paginaInicial));

		sqlEntrada.setSql(sql.toString());

		return sqlEntrada;
	}

	public List<String> getQsaEmpresasInfo(String cnpj) {
		List<String> list = new ArrayList<String>();
		if (cnpj.length() == 14) {
			ResultSet rs = null;
			PreparedStatement ps;
			try {
				Connection conn = this.getConnection();
				StringBuilder sql = new StringBuilder();
				sql.append(
						"SELECT RAZAO_SOCIAL,DT_ABERTURA,SITUACAO ,FANTASIA ,DESC_NATUREZA AS NATUREZA,(SELECT DESC_CNAE FROM CNAE WHERE CNAE = SUBSTR(Q.RAMO_ATVI, 1,7) AND ROWNUM<=1) AS RAMO_ATVI  ");
				sql.append("FROM QSA_EMPRESAS Q WHERE CNPJ = ? AND ROWNUM <= 1");
				ps = conn.prepareStatement(sql.toString());
				ps.setString(1, cnpj);
				rs = ps.executeQuery();
				while (rs != null && rs.next()) {
					list.add(rs.getString("DT_ABERTURA") != null ? rs.getString("DT_ABERTURA") : "");
					list.add(rs.getString("SITUACAO") != null ? rs.getString("SITUACAO") : "");
					list.add(rs.getString("FANTASIA") != null ? rs.getString("FANTASIA") : "");
					list.add(rs.getString("NATUREZA") != null ? rs.getString("NATUREZA") : "");
					list.add(rs.getString("RAMO_ATVI") != null ? rs.getString("RAMO_ATVI") : "");
					list.add(rs.getString("RAZAO_SOCIAL") != null ? rs.getString("RAZAO_SOCIAL") : "");
				}

				if (rs != null && !rs.isClosed())
					rs.close();
				if (ps != null && !ps.isClosed())
					ps.close();

			} catch (Exception ignore) {
				logger.error("Erro no metodo getQsaEmpresasInfo da classe resposta: " + ignore.getMessage());
			} finally {
				this.releaseConnection();
			}
		}
		while (list.size() != 6) {
			list.add("");
		}
		return list;
	}

	// public List<String> getInformacoesInfoSimples(String cnpj){
	// List<String> list=new ArrayList<String>();
	// Data data = new
	// ConsultaInfoSimplesService().getCnpjFromInfoSimples(cnpj);
	//
	// list.add(data.getAbertura_data());
	// list.add(data.getSituacao_cadastral());
	// list.add(data.getNome_fantasia());
	// list.add(data.getNatureza_juridica());
	// list.add(data.getAtividade_economica());
	// list.add(data.getRazao_social());
	//
	// while(list.size()!=6){
	// list.add("");
	// }
	// return list;
	//
	// }

	// Rodrigo Almeida - Subistituído pelo
	// public Empresa getInformacoesWsReceita(String cnpj){
	// List<String> list=new ArrayList<String>();
	// Empresa empresa = new
	// ConsultaWsReceitaService().getCnpjFromInfoWsReceita(cnpj);
	// if(isNullOrEmpty(empresa)){
	// return null;
	// }
	// return empresa;
	// }

	/**
	 * Caso o CEP esteja preenchido, a consulta irá trazer os telefones que estão
	 * associados a matriz
	 *
	 * @param cpfcgc
	 * @param uf
	 * @param cep
	 * @param usuario
	 * @param paginaInicial
	 * @param paginaFinal
	 * @return
	 */
	private SqlToBind montaConsultaCnpjJoinTelefonesAndInfoComplementares(String cpfcgc, UF uf, String cep,
			String usuario, Integer paginaInicial, Integer paginaFinal) {
		StringBuilder sql = new StringBuilder();
		SqlToBind resultado = new SqlToBind();

		sql.append("SELECT * FROM (");
		sql.append("SELECT PAGINA.*,ROWNUM PAGINA_RN FROM (");
		sql.append(
				"SELECT (SELECT STATUS FROM TELEFONES_STATUS WHERE TELEFONE = T.TELEFONE AND ROWNUM <= 1) AS STATUS_LINHA,");
		sql.append(
				" (CASE WHEN (SELECT FLAG FROM TBL_WHATSAPP T2 WHERE T2.TELEFONE = T.TELEFONE AND ROWNUM <= 1) = 1 THEN 1 ELSE 0 END) AS WHATSAPP, ");
		sql.append("(SELECT IS_DATE_CONFIRME(DT_OBITO) FROM OBITO WHERE NU_CPF = CPFCGC AND ROWNUM<=1 ) AS OBITO,");
		sql.append(
				"'' AS NOME,T.PROPRIETARIO AS PROPRIETARIO,T.ATUAL,T.TELEFONE,T.ENDERECO AS ENDERECO,T.NUMERO AS NUMERO,T.COMPLEMENTO,T.TIPO,T.BAIRRO,T.CEP,T.CIDADE,T.UF,T.CPFCGC AS CPFCNPJ,");
		sql.append(
				"i.CPF_CONJUGE AS CPF_CONJUGE,PEGA_NOME_INFO_COMP(I.CPF_CONJUGE) AS NOME_CONJUGE,I.NOME_MAE AS MAE,TO_CHAR(I.DATA_NASC,'DD/MM/YYYY') AS NASC,' ' AS TITULO,T.OPERADORA,TO_CHAR(T.DT_INSTALACAO,'YYYY-MM-DD') AS DT_INSTALACAO, I.SIGNO AS SIGNO ");
		sql.append("FROM TELEFONES T, INFO_COMPLEMENTARES I WHERE T.CPFCGC = I.CPFCNPJ(+) AND T.CPFCGC = ?");
		if (uf != null) {
			sql.append(" AND T.UF = ? ");
		}

		if (cep != null) {
			sql.append(" AND T.CEP = ? ");
		}

		sql.append(" AND ( ROWNUM <= ? )");

		if (cep != null) {
			sql.append(" ORDER BY T.TIPO ASC,TO_NUMBER(ATUAL) DESC, WHATSAPP DESC");
		} else {
			sql.append(" ORDER BY TO_NUMBER(ATUAL) DESC, WHATSAPP DESC");
		}

		sql.append(") PAGINA ");
		
		sql.append(") WHERE  ( PAGINA_RN >= ? AND  PAGINA_RN <= ?) ");
		

		if (cep == null) {
			sql.append(" ORDER BY TO_NUMBER(ATUAL) DESC");
		} else {
			sql.append(" ORDER BY PAGINA_RN,TO_NUMBER(ATUAL) DESC");
		}

		resultado.limpaLista();
		resultado.addString(cpfcgc);
		if (uf != null)
			resultado.addString(uf.getNome());
		if (cep != null)
			resultado.addString(cep);
		resultado.addString(String.valueOf(br.com.confirmeonline.util.SQLConstantes.QTD_MAX_PESQUISA));
		resultado.addString(String.valueOf(paginaInicial));
		resultado.addString(String.valueOf(paginaFinal));


		resultado.setSql(sql.toString());

		return resultado;
	}

	private SqlToBind montaConsultaCnpjFromTelefones(String cpfcgc, UF uf, String usuario, Integer paginaInicial,
			Integer paginaFinal) {
		StringBuilder sql = new StringBuilder();
		SqlToBind resultado = new SqlToBind();

		sql.append(" SELECT * ");
		sql.append(" FROM (SELECT PAGINA.*, ROWNUM PAGINA_RN ");
		sql.append(
				" FROM (  SELECT (SELECT STATUS FROM TELEFONES_STATUS WHERE TELEFONE = T.TELEFONE AND ROWNUM <= 1)AS STATUS_LINHA, ");
		sql.append(
				" (CASE WHEN (SELECT FLAG FROM TBL_WHATSAPP T2 WHERE T2.TELEFONE = T.TELEFONE AND ROWNUM <= 1) = 1 THEN 1 ELSE 0 END) AS WHATSAPP, ");
		sql.append(" (SELECT IS_DATE_CONFIRME(DT_OBITO) FROM OBITO WHERE NU_CPF = T.CPFCGC AND ROWNUM <= 1)AS OBITO, ");
		sql.append(
				" '' AS NOME, T.PROPRIETARIO AS PROPRIETARIO, T.ATUAL,T.TIPO, T.TELEFONE, T.ENDERECO AS ENDERECO, T.NUMERO AS NUMERO, ");
		sql.append(
				" T.COMPLEMENTO, T.BAIRRO,T.CEP,T.CIDADE,T.UF,T.CPFCGC AS CPFCNPJ,'' AS MAE,'' AS NASC,'' AS TITULO,T.OPERADORA, ");
		sql.append(" TO_CHAR (T.DT_INSTALACAO, 'YYYY-MM-DD') AS DT_INSTALACAO, '' AS SIGNO ");
		sql.append(" FROM TELEFONES T ");
		sql.append(" WHERE T.CPFCGC = ? ");
		if (uf != null) {
			sql.append(" AND T.UF = ? ");
		}
		sql.append(" AND (ROWNUM <= ?) ");
		sql.append(" ORDER BY TO_NUMBER (ATUAL) DESC, WHATSAPP DESC) PAGINA) ");
		sql.append(" WHERE (PAGINA_RN >= ? AND PAGINA_RN <= ?) ");
		sql.append(" ORDER BY TO_NUMBER (ATUAL) DESC  ");

		resultado.limpaLista();
		resultado.addString(cpfcgc);
		if (uf != null)
			resultado.addString(uf.getNome());
		resultado.addString(String.valueOf(br.com.confirmeonline.util.SQLConstantes.QTD_MAX_PESQUISA));
		resultado.addString(String.valueOf(paginaInicial));
		resultado.addString(String.valueOf(paginaFinal));

		resultado.setSql(sql.toString());

		return resultado;
	}

	private SqlToBind montaConsultaCnpjFromInfoComplementares(String cpfcgc, UF uf, String usuario,
			Integer paginaInicial, Integer paginaFinal) {
		StringBuilder sql = new StringBuilder();
		SqlToBind resultado = new SqlToBind();

		sql.append(" SELECT * ");
		sql.append(" FROM (SELECT PAGINA.*, ROWNUM PAGINA_RN ");
		sql.append(" FROM (  SELECT '' AS STATUS_LINHA, ");
		sql.append(
				" (CASE WHEN (SELECT FLAG FROM TBL_WHATSAPP T2, TELEFONES T WHERE T2.TELEFONE = T.TELEFONE AND ROWNUM <= 1) = 1 THEN 1 ELSE 0 END) AS WHATSAPP, ");
		sql.append(
				" (SELECT IS_DATE_CONFIRME(DT_OBITO) FROM OBITO WHERE NU_CPF = I.CPFCNPJ AND ROWNUM <= 1) AS OBITO, ");
		sql.append(
				" I.NOME,'' AS PROPRIETARIO,'' AS ATUAL,'' AS TELEFONE,'' AS TIPO,I.ENDERECO AS ENDERECO,I.NUMERO AS NUMERO,I.COMPLEMENTO, ");
		sql.append(
				" i.CPF_CONJUGE AS CPF_CONJUGE,PEGA_NOME_INFO_COMP(I.CPF_CONJUGE) AS NOME_CONJUGE,I.BAIRRO,I.CEP,I.CIDADE,I.UF,I.CPFCNPJ AS CPFCNPJ,I.NOME_MAE AS MAE,TO_CHAR (I.DATA_NASC, 'DD/MM/YYYY') AS NASC, ");
		sql.append(" '' AS TITULO,'' AS OPERADORA,'' AS DT_INSTALACAO,  I.SIGNO AS SIGNO ");
		sql.append(" FROM INFO_COMPLEMENTARES I ");
		sql.append(" WHERE I.CPFCNPJ = ? ");
		if (uf != null) {
			sql.append(" AND I.UF = ?");
		}
		sql.append(" AND (ROWNUM <= ?) ");
		sql.append(" ) PAGINA) ");
		sql.append(" WHERE (PAGINA_RN >= ? AND PAGINA_RN <= ?) ");

		resultado.limpaLista();
		resultado.addString(cpfcgc);
		if (uf != null)
			resultado.addString(uf.getNome());
		resultado.addString(String.valueOf(br.com.confirmeonline.util.SQLConstantes.QTD_MAX_PESQUISA));
		resultado.addString(String.valueOf(paginaInicial));
		resultado.addString(String.valueOf(paginaFinal));

		resultado.setSql(sql.toString());

		return resultado;
	}

	private SqlToBind montaConsultaCnpjFromQsaEmpresas(String cpfcgc, UF uf, String usuario, Integer paginaInicial,
			Integer paginaFinal) {
		StringBuilder sql = new StringBuilder();
		SqlToBind resultado = new SqlToBind();

		sql.append(" SELECT * ");
		sql.append(" FROM (SELECT PAGINA.*, ROWNUM PAGINA_RN ");
		sql.append(" FROM (  SELECT '' AS STATUS_LINHA, ");
		sql.append(" '' AS WHATSAPP, ");
		sql.append(" (SELECT IS_DATE_CONFIRME(DT_OBITO) ");
		sql.append(" FROM OBITO ");
		sql.append(" WHERE NU_CPF = ? AND ROWNUM <= 1) AS OBITO, ");
		sql.append(
				" DT_ABERTURA ,SITUACAO,FANTASIA,DESC_NATUREZA AS NATUREZA,Q.RAZAO_SOCIAL AS NOME,'' AS PROPRIETARIO, ");
		sql.append(
				" '' AS ATUAL,'' AS TELEFONE,'' AS TIPO,Q.ENDERECO AS ENDERECO,Q.NUMERO AS NUMERO,Q.COMPLEMENTO,Q.BAIRRO,Q.CEP,Q.CIDADE,Q.UF,Q.CNPJ AS CPFCNPJ, ");
		sql.append(" '' AS MAE,'' AS NASC,'' AS TITULO,'' AS OPERADORA,'' AS DT_INSTALACAO,'' AS SIGNO ");
		sql.append(" FROM QSA_EMPRESAS Q ");
		sql.append(" WHERE Q.CNPJ = ? ");
		if (uf != null) {
			sql.append(" AND Q.UF = ?");
		}
		sql.append(" AND (ROWNUM <= ?) ");
		sql.append(" ) PAGINA) ");
		sql.append(" WHERE (PAGINA_RN >= ? AND PAGINA_RN <= ?) ");

		resultado.limpaLista();
		resultado.addString(cpfcgc);
		resultado.addString(cpfcgc);
		if (uf != null)
			resultado.addString(uf.getNome());
		resultado.addString(String.valueOf(br.com.confirmeonline.util.SQLConstantes.QTD_MAX_PESQUISA));
		resultado.addString(String.valueOf(paginaInicial));
		resultado.addString(String.valueOf(paginaFinal));

		resultado.setSql(sql.toString());

		return resultado;
	}

	private SqlToBind montaConsultaCnpjMatrizFromQsaEmpresas(String cpfcgc, UF uf, String usuario,
			Integer paginaInicial, Integer paginaFinal) {
		StringBuilder sql = new StringBuilder();
		SqlToBind resultado = new SqlToBind();

		sql.append(" SELECT * ");
		sql.append(" FROM (SELECT PAGINA.*, ROWNUM PAGINA_RN ");
		sql.append(" FROM (  SELECT '' AS STATUS_LINHA, ");
		sql.append(" '' AS WHATSAPP, ");
		sql.append(" (SELECT IS_DATE_CONFIRME(DT_OBITO) ");
		sql.append(" FROM OBITO ");
		sql.append(" WHERE NU_CPF = ? AND ROWNUM <= 1) AS OBITO, ");
		sql.append(
				" DT_ABERTURA ,SITUACAO,FANTASIA,DESC_NATUREZA AS NATUREZA,Q.RAZAO_SOCIAL AS NOME,'' AS PROPRIETARIO, ");
		sql.append(
				" '' AS ATUAL,'' AS TELEFONE,'' AS TIPO,Q.ENDERECO AS ENDERECO,Q.NUMERO AS NUMERO,Q.COMPLEMENTO,Q.BAIRRO,Q.CEP,Q.CIDADE,Q.UF,Q.CNPJ AS CPFCNPJ, ");
		sql.append(" '' AS MAE,'' AS NASC,'' AS TITULO,'' AS OPERADORA,'' AS DT_INSTALACAO,'' AS SIGNO ");
		sql.append(" FROM QSA_EMPRESAS Q ");
		sql.append(" WHERE Q.CNPJ = ? ");
		if (uf != null) {
			sql.append(" AND Q.UF = ?");
		}
		sql.append(" AND (ROWNUM <= ?) ");
		sql.append(" ) PAGINA) ");
		sql.append(" WHERE (PAGINA_RN >= ? AND PAGINA_RN <= ?) ");

		resultado.limpaLista();
		resultado.addString(cpfcgc);
		resultado.addString(cpfcgc);

		if (uf != null)
			resultado.addString(uf.getNome());
		resultado.addString(String.valueOf(br.com.confirmeonline.util.SQLConstantes.QTD_MAX_PESQUISA));
		resultado.addString(String.valueOf(paginaInicial));
		resultado.addString(String.valueOf(paginaFinal));

		resultado.setSql(sql.toString());

		return resultado;
	}

	private SqlToBind montaConsultaCpfJoinTelefonesAndInfoComplementares(String cpfcgc, UF uf, String usuario,
			Integer paginaInicial, Integer paginaFinal) {
		StringBuilder sql = new StringBuilder();
		SqlToBind resultado = new SqlToBind();

		sql.append("SELECT * FROM (");
		sql.append("SELECT PAGINA.*,ROWNUM PAGINA_RN FROM (");
		sql.append(
				"SELECT (SELECT STATUS FROM TELEFONES_STATUS WHERE TELEFONE = T.TELEFONE AND ROWNUM <= 1) AS STATUS_LINHA,");
		sql.append(
				" (CASE WHEN (SELECT FLAG FROM TBL_WHATSAPP T2 WHERE T2.TELEFONE = T.TELEFONE AND ROWNUM <= 1) = 1 THEN 1 ELSE 0 END) AS WHATSAPP, ");
		sql.append("(SELECT IS_DATE_CONFIRME(DT_OBITO) FROM OBITO WHERE NU_CPF = ? AND ROWNUM<=1 ) AS OBITO,");
		sql.append("(SELECT DT_ABERTURA FROM QSA_EMPRESAS WHERE CNPJ = ? AND ROWNUM <= 1) AS DT_ABERTURA,");
		sql.append("(SELECT SITUACAO FROM QSA_EMPRESAS WHERE CNPJ = ? AND ROWNUM <= 1) AS SITUACAO,");
		sql.append("(SELECT FANTASIA FROM QSA_EMPRESAS WHERE CNPJ = ? AND ROWNUM <= 1) AS FANTASIA,");
		sql.append("(SELECT DESC_NATUREZA FROM QSA_EMPRESAS WHERE CNPJ = ? AND ROWNUM <= 1) AS NATUREZA,");
		sql.append(
				"'' AS NOME,case when i.nome is null then t.proprietario else i.nome end as proprietario,T.ATUAL,T.TELEFONE,T.ENDERECO AS ENDERECO,T.NUMERO AS NUMERO,T.TIPO,T.COMPLEMENTO,T.BAIRRO,T.CEP,T.CIDADE,T.UF,I.CPFCNPJ AS CPFCNPJ,");
		sql.append(
				"i.CPF_CONJUGE AS CPF_CONJUGE,PEGA_NOME_INFO_COMP(I.CPF_CONJUGE) AS NOME_CONJUGE,I.NOME_MAE AS MAE,I.NOME_PAI AS PAI,TO_CHAR(I.DATA_NASC,'DD/MM/YYYY') AS NASC,' ' AS TITULO,T.OPERADORA,TO_CHAR(T.DT_INSTALACAO,'YYYY-MM-DD') AS DT_INSTALACAO, I.SIGNO AS SIGNO ");
		sql.append("FROM INFO_COMPLEMENTARES I ,TELEFONES T WHERE I.CPFCNPJ = T.CPFCGC(+) AND I.CPFCNPJ = ?");
		if (uf != null) {
			sql.append(" AND T.UF = ? ");
		}
		sql.append(" AND ( ROWNUM <= ? ) ORDER BY TO_NUMBER(ATUAL) DESC");
		sql.append(") PAGINA" +
				" union all\n" +
				" (select null \n" +
				"as STATUS_LINHA,\n" +
				" null \n" +
				"as WHATSAPP,\n" +
				"null \n" +
				"as OBITO,\n" +
				" null \n" +
				"as DT_ABERTURA,\n" +
				"null \n" +
				"as SITUACAO,\n" +
				"null \n" +
				"as FANTASIA,\n" +
				"null \n" +
				"as NATUREZA,\n" +
				"INFO.NOME \n" +
				"as NOME,\n" +
				"null  as PROPRIETARIO,\n" +
				"'0'  as ATUAL, null  as TELEFONE,\n" +
				"INFO.ENDERECO \n" +
				"as ENDERECO, TO_NUMBER(INFO.NUMERO) \n" +
				"as NUMERO, INFO.TIPOLOG  as TIPO, INFO.COMPLEMENTO  as COMPLEMENTO, INFO.BAIRRO  as BAIRRO,\n" +
				"INFO.CEP  as CEP, INFO.CIDADE as CIDADE, INFO.UF as UF, INFO.CPFCNPJ as CPFCNPJ, null as CPF_CONJUGE,null \n" +
				"as NOME_CONJUGE, null as MAE, null as PAI, null  as NASC, null as TITULO,null  as OPERADORA, null as DT_INSTALACAO, null as SIGNO, ROWNUM as PAGINA_RN\n" +
				"\n" +
				"from dbcred.INFO_COMPLEMENTARES INFO where cpfcnpj = ?) ");
		sql.append(") WHERE  ( PAGINA_RN >= ? AND  PAGINA_RN <= ? ) ORDER BY TO_NUMBER(ATUAL) DESC, WHATSAPP DESC");

		resultado.limpaLista();
		resultado.addString(cpfcgc);
		resultado.addString(cpfcgc);
		resultado.addString(cpfcgc);
		resultado.addString(cpfcgc);
		resultado.addString(cpfcgc);
		resultado.addString(cpfcgc);

		if (uf != null)
			resultado.addString(uf.getNome());
		resultado.addString(String.valueOf(br.com.confirmeonline.util.SQLConstantes.QTD_MAX_PESQUISA));
		resultado.addString(cpfcgc);
		resultado.addString(String.valueOf(paginaInicial));
		resultado.addString(String.valueOf(paginaFinal));

		resultado.setSql(sql.toString());

		return resultado;
	}

	private SqlToBind montaConsultaCpfFromTelefones(String cpfcgc, UF uf, String usuario, Integer paginaInicial,
			Integer paginaFinal) {
		StringBuilder sql = new StringBuilder();
		SqlToBind resultado = new SqlToBind();

		sql.append(" SELECT * ");
		sql.append(" FROM (SELECT PAGINA.*, ROWNUM PAGINA_RN ");
		sql.append(
				" FROM (  SELECT (SELECT STATUS FROM TELEFONES_STATUS WHERE TELEFONE = T.TELEFONE AND ROWNUM <= 1) AS STATUS_LINHA, ");
		sql.append(
				" (CASE WHEN (SELECT FLAG FROM TBL_WHATSAPP T2 WHERE T2.TELEFONE = T.TELEFONE AND ROWNUM <= 1) = 1 THEN 1 ELSE 0 END) AS WHATSAPP, ");
		sql.append(" (SELECT IS_DATE_CONFIRME(DT_OBITO) FROM OBITO WHERE NU_CPF = T.CPFCGC AND ROWNUM <= 1)AS OBITO, ");
		sql.append(" (SELECT DT_ABERTURA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS DT_ABERTURA, ");
		sql.append(" (SELECT SITUACAO FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS SITUACAO, ");
		sql.append(" (SELECT FANTASIA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1) AS FANTASIA, ");
		sql.append(" (SELECT DESC_NATUREZA FROM QSA_EMPRESAS WHERE CNPJ = T.CPFCGC AND ROWNUM <= 1)AS NATUREZA, ");
		sql.append(
				" '' AS NOME,T.PROPRIETARIO AS PROPRIETARIO,T.ATUAL,T.TELEFONE,T.ENDERECO AS ENDERECO,T.NUMERO AS NUMERO,T.COMPLEMENTO, ");
		sql.append(
				" T.BAIRRO,T.TIPO,T.CEP,T.CIDADE,T.UF,T.CPFCGC AS CPFCNPJ,'' AS MAE, '' AS PAI, '' AS NOME_CONJUGE, '' AS CPF_CONJUGE, '' AS NASC,'' AS TITULO,T.OPERADORA, ");
		sql.append(" TO_CHAR (T.DT_INSTALACAO, 'YYYY-MM-DD') AS DT_INSTALACAO, '' AS SIGNO ");
		sql.append(" FROM TELEFONES T ");
		sql.append(" WHERE T.CPFCGC = ? ");
		if (uf != null) {
			sql.append(" AND T.UF = ? ");
		}
		sql.append(" AND (ROWNUM <= ?) ");
		sql.append(" ORDER BY TO_NUMBER (ATUAL) DESC) PAGINA) ");
		sql.append(" WHERE (PAGINA_RN >= ? AND PAGINA_RN <= ?) ");
		sql.append(" ORDER BY TO_NUMBER (ATUAL) DESC,  WHATSAPP DESC");

		resultado.limpaLista();
		resultado.addString(cpfcgc);
		if (uf != null)
			resultado.addString(uf.getNome());
		resultado.addString(String.valueOf(br.com.confirmeonline.util.SQLConstantes.QTD_MAX_PESQUISA));
		resultado.addString(String.valueOf(paginaInicial));
		resultado.addString(String.valueOf(paginaFinal));

		resultado.setSql(sql.toString());

		return resultado;
	}

	private SqlToBind montaConsultaCpfFromInfoComplementares(String cpfcgc, UF uf, String usuario,
			Integer paginaInicial, Integer paginaFinal) {
		StringBuilder sql = new StringBuilder();
		SqlToBind resultado = new SqlToBind();

		sql.append(" SELECT * ");
		sql.append(" FROM (SELECT PAGINA.*, ROWNUM PAGINA_RN ");
		sql.append(" FROM (  SELECT '' AS STATUS_LINHA, ");
		sql.append(" '' AS WHATSAPP, ");
		sql.append(" (SELECT IS_DATE_CONFIRME(DT_OBITO) FROM OBITO WHERE NU_CPF = ? AND ROWNUM <= 1)AS OBITO, ");
		sql.append(" (SELECT DT_ABERTURA FROM QSA_EMPRESAS WHERE CNPJ = I.CPFCNPJ AND ROWNUM <= 1) AS DT_ABERTURA, ");
		sql.append(" (SELECT SITUACAO FROM QSA_EMPRESAS WHERE CNPJ = I.CPFCNPJ AND ROWNUM <= 1) AS SITUACAO, ");
		sql.append(" (SELECT FANTASIA FROM QSA_EMPRESAS WHERE CNPJ = I.CPFCNPJ AND ROWNUM <= 1) AS FANTASIA, ");
		sql.append(" (SELECT DESC_NATUREZA FROM QSA_EMPRESAS WHERE CNPJ = I.CPFCNPJ AND ROWNUM <= 1)AS NATUREZA, ");
		sql.append(
				" I.NOME,'' AS PROPRIETARIO,'' AS ATUAL,'' AS TELEFONE,I.ENDERECO AS ENDERECO,I.NUMERO AS NUMERO,I.COMPLEMENTO, ");
		sql.append(
				" i.CPF_CONJUGE AS CPF_CONJUGE,PEGA_NOME_INFO_COMP(I.CPF_CONJUGE) AS NOME_CONJUGE,I.BAIRRO,I.CEP,I.CIDADE,I.UF,I.CPFCNPJ AS CPFCNPJ,I.NOME_MAE AS MAE,TO_CHAR (I.DATA_NASC, 'DD/MM/YYYY') AS NASC, ");
		sql.append(" ' ' AS TITULO,'' AS OPERADORA,'' AS DT_INSTALACAO, I.SIGNO AS SIGNO ");
		sql.append(" FROM INFO_COMPLEMENTARES I ");
		sql.append(" WHERE I.CPFCNPJ = ? ");
		if (uf != null) {
			sql.append(" AND I.UF = ? ");
		}
		sql.append(" AND (ROWNUM <= ? ) ");
		sql.append(" ) PAGINA) ");
		sql.append(" WHERE (PAGINA_RN >= ? AND PAGINA_RN <= ? ) ");

		resultado.limpaLista();
		resultado.addString(cpfcgc);
		resultado.addString((cpfcgc));
		if (uf != null)
			resultado.addString(uf.getNome());
		resultado.addString(String.valueOf(br.com.confirmeonline.util.SQLConstantes.QTD_MAX_PESQUISA));
		resultado.addString(String.valueOf(paginaInicial));
		resultado.addString(String.valueOf(paginaFinal));

		resultado.setSql(sql.toString());

		return resultado;
	}

	private Boolean isConsultaCpfCnpjValida() {
		return !estaVazioOuNulo(this.infocomplementares.getNome());
	}

	public void limpaQueries(StringBuilder... builders) {
		for (StringBuilder b : builders) {
			if (b != null) {
				b.delete(0, b.length());
			}
		}
	}

	public List<Telefone> findTelefoneByCpf(String cpfcnpj, String qtdPesq, LoginMBean mx) {
		/*
		 * StringBuilder sql1 = new StringBuilder(); StringBuilder sql2 = new
		 * StringBuilder(); StringBuilder sql3 = new StringBuilder();
		 * 
		 * sql1 = montaConsultaCpfJoinTelefonesAndInfoComplementares(cpfcnpj, null,
		 * null, 1, Integer.valueOf(qtdPesq)); sql2 =
		 * montaConsultaCpfFromTelefones(cpfcnpj, null, null, 1,
		 * Integer.valueOf(qtdPesq)); sql3 =
		 * montaConsultaCpfFromInfoComplementares(cpfcnpj, null, null, 1,
		 * Integer.valueOf(qtdPesq));
		 * 
		 * List<Telefone> telefonesFilho;
		 * 
		 * telefonesFilho = findTelefones(mx, sql1.toString());
		 * 
		 * if (!(telefonesFilho.size() > 0)) { telefonesFilho = findTelefones(mx,
		 * sql2.toString()); } if (!(telefonesFilho.size() > 0)) { telefonesFilho =
		 * findTelefones(mx, sql3.toString()); }
		 */
		// return telefonesFilho;
		return null;

	}

	public Boolean pesquisaOperadora(LoginMBean mb) {
		mb.setResposta_cep(false);
		mb.setResposta_consulta(false);
		mb.setResposta_conArmazenada(false);
		mb.setResposta_endereco(false);
		mb.setResposta_historico_credito(false);
		mb.setResposta_mapa(false);
		mb.setResposta_nome(false);
		mb.setResposta_razao(false);
		mb.getPessoaSite().setCpfcnpj("");
		this.setForm_active_cpfcnpj("form");
		this.setForm_active_telefone("form");
		this.setForm_active_cep("form");
		this.setForm_active_endereco("form");
		this.setForm_active_operadora("form active");
		this.setForm_active_historico_credito("form");
		this.setForm_active_nome("form");
		this.setForm_active_razao_social("form");
		this.setForm_active_veiculos("form");

		Boolean achou = false;

		this.telefoneOperadora = Search.searchByTelefone(mb.getPessoaSite().getTelefoneOperadora());

		achou = !estaVazioOuNulo(this.telefoneOperadora.getOperadora());

		String[] nomeservidor = mb.getServidor();
		if (achou) {
			Conexao.registraConsulta(this.getConnection(), "OPERADORA", mb.getPessoaSite().getTelefone(),
					mb.getUsuario().getLogin(), mb.getUsuario().getSenha(), "CONFI", mb.getUsuario().getIP(),
					nomeservidor[2]);
		}

		mb.setResposta_operadora(achou);
		releaseConnection();
		return achou;
	}

	public void pesquisaOperadora(LoginMBean mb, String telefone) {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
		mensagem = "";
		try {

			mb.setResposta_cep(false);
			mb.setResposta_consulta(false);
			mb.setResposta_conArmazenada(false);
			mb.setResposta_endereco(false);
			mb.setResposta_historico_credito(false);
			mb.setResposta_mapa(false);
			mb.setResposta_nome(false);
			mb.setResposta_razao(false);
			mb.getPessoaSite().setCpfcnpj("");
			this.setForm_active_cpfcnpj("form");
			this.setForm_active_telefone("form");
			this.setForm_active_cep("form");
			this.setForm_active_endereco("form");
			this.setForm_active_operadora("form active");
			this.setForm_active_historico_credito("form");
			this.setForm_active_nome("form");
			this.setForm_active_razao_social("form");
			this.setForm_active_veiculos("form");
			this.telefoneOperadora = null;

			Boolean achou = false;
			if (!estaVazioOuNulo(telefone)) {
				telefone = Telefone.formatTelefone(telefone);
				this.telefoneOperadora = Search.searchByTelefone(telefone);
			}

			achou = !estaVazioOuNulo(this.telefoneOperadora.getOperadora());

			String[] nomeservidor = mb.getServidor();
			if (achou) {
				Conexao.registraConsulta(this.getConnection(), "OPERADORA", mb.getPessoaSite().getTelefone(),
						mb.getUsuario().getLogin(), mb.getUsuario().getSenha(), "CONFI", mb.getUsuario().getIP(),
						nomeservidor[2]);
			}

			mb.setResposta_operadora(achou);

			if (!achou && menorDeIdade==false) {
				mensagem = "Registro Não Encontrado";
			}

			FacesMessage msg = new FacesMessage(mensagem);
			facesContext.addMessage("form", msg);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			releaseConnection();
		}

	}

	public List<Integer> getPaginas(Integer pagina, Integer comando, LoginMBean mb) {
		List<Integer> paginas = new ArrayList<Integer>();
		String qtdpesq = mb.getQtdpesq();
		Integer paginaFinal = 0;
		Integer paginaInicial = 0;

		if (Integer.parseInt(qtdpesq) < 1) {
			qtdpesq = "1";
		}
		if (Integer.parseInt(qtdpesq) > 100) {
			qtdpesq = "100";
		}
		/* Avança registro */

		/*
		 * Se o comando 0 for passado, sempre vai iniciar a pesquisa do inicio
		 */

		if (comando == 1) {
			pagina = pagina + 1;
		}

		/* Volta registro */
		if (comando == 2) {
			pagina = pagina - 1;
		}

		paginaFinal = Integer.parseInt(qtdpesq) * pagina;
		paginaInicial = (paginaFinal - Integer.parseInt(qtdpesq)) + 1;

		if (pagina == 1 || pagina < 1) {
			paginaFinal = Integer.parseInt(qtdpesq);
			paginaInicial = 1;
			pagina = 1;
		}

		/* 250 e hardcode e o maximo de registros que pesquisamos */
		if (pagina > (250 / Integer.parseInt(qtdpesq))) {
			pagina = 250 / Integer.parseInt(qtdpesq);
			paginaFinal = 250;
			paginaInicial = (paginaFinal - Integer.parseInt(qtdpesq)) + 1;
		}

		if (comando == 0) {
			pagina = 1;
			paginaFinal = Integer.parseInt(qtdpesq);
			paginaInicial = 1;
		}
		paginaFinal += 1;

		paginas.add(paginaInicial);
		paginas.add(paginaFinal);
		paginas.add(pagina);

		return paginas;
	}

	public void limpaPesquisaAnterior(LoginMBean mb) {
		emails = new ArrayList<Emails>();
		telefone = new ArrayList<Telefone>();
		telefoneComercial = new ArrayList<TelefonesComerciais>();
		telefoneReferencia = new ArrayList<TelefonesReferencia>();
		parentes = new ArrayList<Parentes>();
		obito = null;
		moradores = new ArrayList<Moradores>();
		vizinhos = new ArrayList<Vizinhos>();
		filhos = new ArrayList<Parentes>();
		veiculos = new ArrayList<Veiculo>();
		imoveis = new ArrayList<Imoveis>();
		sociedades = new ArrayList<Sociedades>();
		socios = new ArrayList<Socios>();
		obitoNacional = new Obito();
		mb.setM_celulares(false);
		mb.setM_cep_geral(false);
		mb.setM_email(false);
		mb.setM_filhos(false);
		mb.setM_imoveis(false);
		mb.setM_moradores(false);
		mb.setM_obito(false);
		mb.setM_parentes(false);
		mb.setM_obitos(false);
		mb.setM_sociedades(false);
		mb.setM_socios(false);
		mb.setM_tel_comercial(false);
		mb.setM_telefone_referencia(false);
		mb.setM_veiculo(false);
		mb.setM_vizinho(false);
		mb.setM_obitoNacional(false);
	}

	private List<Vizinhos> removeDuplicadas(List<Vizinhos> list) {
		List<Vizinhos> uniqueList = new ArrayList<>();
		for (Vizinhos viz : list) {
			Iterator<Vizinhos> it = list.iterator();
			while (it.hasNext()) {
				Vizinhos v = it.next();
				if (viz.equals(v) && !uniqueList.contains(v)) {
					uniqueList.add(viz);
				}
			}
		}
		return uniqueList;
	}

	public List<SocioSociedade> findSocioSociedadesByCpfcgc(String cpfcgc) throws SQLException {
		List<SocioSociedade> socioSociedades = new ArrayList<>();
		Connection conn = null;
		String sql = "";
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			conn = this.getConnection();

			if (cpfcgc.length() == 14)
				sql = "SELECT CPF AS CPFCGC,NOME FROM QSA_SOCIOS WHERE CNPJ = ? AND ROWNUM <=50";
			else
				sql = "SELECT CNPJ AS CPFCGC, (SELECT RAZAO_SOCIAL FROM QSA_EMPRESAS WHERE  CNPJ = Q.CNPJ ) AS NOME FROM QSA_SOCIOS Q WHERE CPF = ?  AND ROWNUM <=50";

			ps = conn.prepareStatement(sql);

			ps.setString(1, cpfcgc);

			rs = ps.executeQuery();

			while (rs != null && rs.next()) {
				socioSociedades.add(new SocioSociedade(rs.getString("CPFCGC") != null ? rs.getString("CPFCGC") : "",
						rs.getString("NOME") != null ? rs.getString("NOME") : ""));
			}

		} catch (Exception ignore) {
			logger.error("Erro no metodo findSocioSociedadesByCpfcgc da classe Respossta: " + ignore.getMessage());

		} finally {
			releaseConnection();
			if (rs != null && !rs.isClosed())
				rs.close();
			if (ps != null && !ps.isClosed())
				ps.close();
		}

		return socioSociedades;
	}

	public List<String> findEmailsByCpfcgc(String cpfcgc) throws SQLException {
		List<String> emails = new ArrayList<>();
		Connection conn = null;
		String sql = "";
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = this.getConnection();

			sql = "SELECT EM_NM_EMAIL FROM DM_EMAIL WHERE CPF_CNPJ = ? AND ROWNUM < 10 ORDER BY ID_VALIDADO ASC";

			ps = conn.prepareStatement(sql);

			ps.setString(1, cpfcgc);

			rs = ps.executeQuery();

			while (rs != null && rs.next()) {
				if (!emails.contains(rs.getString("EM_NM_EMAIL").trim()))
					emails.add(rs.getString("EM_NM_EMAIL").trim());
			}

		} catch (Exception ignore) {
			logger.error("Erro no metodo findEmailsByCpfcgc da classe Resposta: " + ignore.getMessage());
		} finally {

			releaseConnection();
			if (rs != null && !rs.isClosed())
				rs.close();
			if (ps != null && !ps.isClosed())
				ps.close();
		}

		return emails;
	}

	private String formatDate(Date dateNasc, String pattern) {
		String returnDate = "";
		try {
			SimpleDateFormat df = new SimpleDateFormat(pattern);
			returnDate = df.format(dateNasc);
		} catch (Exception e) {
			returnDate = "";
		}
		return returnDate;
	}

	public boolean isNullOrEmpty(Object o) {
		return o == null || o.toString().equals("");
	}

	private boolean isProtected(String cpfcnpj, String login) {
		Boolean achou = false;
		Connection conn = null;
		String sql = "";
		try {
			conn = this.getConnection();

			sql = "SELECT 'TRUE' FROM PROTECAO_CPFCNPJ WHERE CPFCGC = ? AND USUARIO = ? ";

			PreparedStatement ps = conn.prepareStatement(sql);

			ps.setString(1, cpfcnpj);
			ps.setString(2, login);

			ResultSet rs = ps.executeQuery();

			while (rs != null && rs.next()) {
				achou = true;
			}

			if (rs != null && !rs.isClosed())
				rs.close();
			if (ps != null && !ps.isClosed())
				ps.close();

		} catch (Exception ignore) {
			logger.error("Erro no metodo isProtected da classe Resposta: " + ignore.getMessage());
		} finally {
			releaseConnection();
		}

		return achou;
	}

	Obito pesquisaObitoNacional(LoginMBean mb, PessoaObitoDTO pessoaObito) {
		if (mb.getUsuario().getObitoNacional()) {
			try {
				limpaPesquisaAnterior(mb);
				this.connection = getConnection();
				this.possuiConexao = true;

				this.obitoNacional = ObitoDao.findObitoCompletoByPessoa(pessoaObito, connection);
				new Conexao().registraConsultaObito(connection, "CONFIRMEONLINE", pessoaObito,
						mb.getUsuario().getLogin(), mb.getUsuario().getIP());
			} catch (Exception e) {
				logger.error("Erro no metodo pesquisaObitoNacional da classe: " + e.getMessage());
				this.obitoNacional = new Obito();
				this.obitoNacional.setStatusLocalizacao(Obito.StatusLocalizacao.NAO_LOCALIZADO);

			} finally {
				this.exibirMensagem = true;
				this.possuiConexao = false;
				releaseConnection();
			}
		} else {
			this.obitoNacional = new Obito();
			this.obitoNacional.setStatusLocalizacao(Obito.StatusLocalizacao.NAO_AUTORIZADO);
		}

		return obitoNacional;

	}

	private void verificaNomesDivergentes(Infocomplementares dadosBasicos, Infocomplementares info, String cpfcnpj,
			String telefone, Connection conn) {
		try {
			if (!isNullOrEmpty(cpfcnpj) && !isNullOrEmpty(telefone)) {
				String sql = "SELECT CPFCGC, TELEFONE, NOME_TEL, NOME_INFO FROM TEL_INFO_NOME_DIF WHERE CPFCGC=? AND TELEFONE=?";

				PreparedStatement ps = conn.prepareStatement(sql);
				ps.setString(1, cpfcnpj);
				ps.setString(2, telefone);

				ResultSet rs = ps.executeQuery();

				if (rs != null && rs.next()) {
					dadosBasicos.setNome(rs.getString("NOME_INFO"));
					info.setNome(rs.getString("NOME_INFO"));
					info.setNomeTelefone(rs.getString("NOME_TEL"));
				}

				ps.close();
				rs.close();
			}

		} catch (Exception ignore) {
			logger.error("Erro no metodo verificaNomesDivergentes da classe: " + ignore.getMessage());
		}
	}

	private String getTituloEleitoral(String cpfcnpj, Connection conn) {

		String titulo = "";

		try {
			if (!isNullOrEmpty(cpfcnpj) && !isNullOrEmpty(telefone)) {
				String sql = "SELECT TITULO FROM DBCRED.TITULO_ELEITORAL WHERE CPF=? ";

				PreparedStatement ps = conn.prepareStatement(sql);
				ps.setString(1, cpfcnpj);

				ResultSet rs = ps.executeQuery();

				if (rs != null && rs.next()) {

					titulo = rs.getString("TITULO");

				}

				ps.close();
				rs.close();

			}

		} catch (Exception ignore) {
			logger.error("Erro no metodo getTituloEleitoral da classe Resposta: " + ignore.getMessage());
			return "";
		}

		return titulo;

	}

	private String retornaOperadorasSelecionadas() {
		StringBuilder operadorasSelecionadas = new StringBuilder();
		for (String operadora : operadoras) {
			operadorasSelecionadas.append("'" + operadora + "'" + ",");
		}

		return operadorasSelecionadas.deleteCharAt(operadorasSelecionadas.length() - 1).toString();
	}

	public EmpresaVo retornaCnpjCepMatriz(String cpfcnpj, Connection conn) {
		EmpresaVo empresaVo = new EmpresaVo();

		try {
			if (!isNullOrEmpty(cpfcnpj) && !isNullOrEmpty(telefone)) {
				String sql = "SELECT * FROM QSA_EMPRESAS WHERE CNPJ LIKE ? AND TIPO_FILIAL = 'MATRIZ'";

				PreparedStatement ps = conn.prepareStatement(sql);
				ps.setString(1, cpfcnpj.substring(0, 8) + "%");

				ResultSet rs = ps.executeQuery();

				if (rs != null && rs.next()) {
					empresaVo.setCnpj(rs.getString("CNPJ"));
					empresaVo.setCep(rs.getString("CEP"));
				}

				ps.close();
				rs.close();

			}

		} catch (Exception ignore) {
			logger.error("Erro no metodo retornaCnpjCepMatriz da classe Resposta: " + ignore.getMessage());
			return null;
		}

		return empresaVo;

	}

	/**
	 * Retorna o Status para a busca no info simples na tabela de controle
	 * INT_SWITCH
	 *
	 * @return
	 */
	public Boolean habilitaInfoSimples() {
		Connection conn = this.getConnection();

		Statement statement;
		ResultSet resultSet;
		Integer valor = null;

		try {
			statement = conn.createStatement();
			resultSet = statement.executeQuery("SELECT * FROM INT_SWITCH");

			while (resultSet.next()) {
				valor = resultSet.getInt("INFO_SIMPLES");
			}

			if (valor == 1) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}

	}

	/**
	 * Retorna o Status para a busca no ws receita na tabela de controle INT_SWITCH
	 *
	 * @return
	 */
	public Boolean habilitaWsReceita() {
		Connection conn = this.getConnection();

		Statement statement;
		ResultSet resultSet;
		Integer valor = null;
		Boolean resultado;
		try {
			statement = conn.createStatement();
			resultSet = statement.executeQuery("SELECT * FROM INT_SWITCH");

			while (resultSet.next()) {
				valor = resultSet.getInt("WS_RECEITA");
			}

			if (valor == 1) {
				resultado = true;
			} else {
				resultado = false;
			}

			if (resultSet != null && !resultSet.isClosed())
				resultSet.close();
			if (statement != null && !statement.isClosed())
				statement.close();

			return resultado;
		} catch (Exception e) {
			logger.error("Erro no metodo habilitaWsReceita da classe resposta: " + e.getMessage());
			return false;
		}

	}

	public void buscaInfoWSReceitaPJ(LoginMBean mb) {
		Boolean ok = false;

		if (mb.getPessoaSite().getCpfcnpj().length() == 14) {
			Empresa empresa = null;

			if (habilitaWsReceita()) {
				// empresa =
				// this.getInformacoesWsReceita(mb.getPessoaSite().getCpfcnpj());
				// Rodrigo Almeida - 11/12/2017
				empresa = new ConsultaReceitaService().listarDadosReceitaByCNPJ(mb.getPessoaSite().getCpfcnpj());
			}

			if (!isNullOrEmpty(empresa)) {
				this.setLabel1("Dt. Fundação:");
				this.infocomplementares.setDtnasc(empresa.getDataAbertura() == null ? "" : empresa.getDataAbertura());

				this.infocomplementares.setSigno(empresa.getNomeFantasia() == null ? "" : empresa.getNomeFantasia());
				this.setLabel2("Nome Fantasia:");

				this.infocomplementares
						.setSexo(empresa.getNaturezaJuridica() == null ? "" : empresa.getNaturezaJuridica());
				this.setLabel3("Natureza:");

				this.infocomplementares
						.setNomemae(empresa.getSituacaoCadastral() == null ? "" : empresa.getSituacaoCadastral());
				this.setLabel4("Situação:");
				this.setLabel5("");
				this.infocomplementares
						.setRamoAtvi(empresa.getCnaeprincipal() == null ? "" : empresa.getCnaeprincipal());

				try {
					new PessoaJuridicaDAO(getConnection()).registraRetornoQSAEmpresa(empresa);
				} catch (SQLException e) {
					logger.error("Erro no metodo buscaInfoWSReceitaPJ da classe Resposta " + e.getMessage());
				}

				ok = true;
			} else {
				ok = false;
			}
		}
	}

	private boolean useStatusPhone() throws SQLException {
		Connection conn = this.getConnection();

		Statement statement = null;
		ResultSet resultSet = null;
		Integer valor = null;
		boolean resultado = false;

		try {
			statement = conn.createStatement();
			resultSet = statement.executeQuery("SELECT * FROM INT_SWITCH");

			while (resultSet.next()) {
				valor = resultSet.getInt("BLOQUEIO_IP_FIXO");
			}

			if (valor == 1) {
				resultado = true;
			} else {
				resultado = false;
			}

			return resultado;

		} catch (Exception e) {
			logger.error("Erro no metodo useStatusPhone da classe Resposta: " + e.getMessage());
			return false;
		} finally {
			if (resultSet != null && !resultSet.isClosed())
				resultSet.close();
			if (statement != null && !statement.isClosed())
				statement.close();
		}
	}

	public Boolean getUseStatus() {
		try {
			if (useStatus == null) {
				useStatus = useStatusPhone();
			}
			return useStatus;
		} catch (Exception e) {
			return false;
		}
	}

	private boolean possuiRestricao(String cpf) throws SQLException {
		ResultSet resultSet = null;
		PreparedStatement preparedStatement = null;
		Connection conn = this.getConnection();
		try {
			preparedStatement = conn.prepareStatement(
					"SELECT * FROM RESTRICAO_BANCARIA WHERE REST_CD_CPF_CNPJ = ? AND REST_TP_SITUACAO = 'A' ");
			preparedStatement.setString(1, cpf);
			resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
				return true;
			}
			return false;
		} catch (Exception e) {
			logger.error("Erro no metodo possuiRestricao da classe Resposta: " + e.getMessage());
			return false;
		} finally {
			preparedStatement.close();
			resultSet.close();
			conn.close();
		}
	}

	private boolean possuiAlertas(String cpf) throws SQLException {
		ResultSet resultSet = null;
		PreparedStatement preparedStatement = null;
		Connection conn = this.getConnection();
		try {

			preparedStatement = conn
					.prepareStatement("SELECT * FROM ALERTAS WHERE ALER_CD_CPF_CNPJ = ? AND SITUACAO = 'A'");
			preparedStatement.setString(1, cpf);
			resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
				return true;
			}

			return false;
		} catch (Exception e) {
			logger.error("Erro no metodo possuiAlertas da classe Resposta: " + e.getMessage());
			return false;
		} finally {
			preparedStatement.close();
			resultSet.close();
			conn.close();
		}

	}

	public boolean verificaProtecaoDadosPessoais(String cpf, String nome) throws SQLException {
		StringBuilder sql = new StringBuilder();
//        ResultSet rs = null;
//        PreparedStatement stmt = null;
//        Connection conn = this.getConnection();

		sql.append(" SELECT pdp.cpf, pdp.nome, pdp.data_bloqueio, pdp.status ");
		sql.append("   from DBCRED.TB_PROTECAO_DADOS_PESSOAIS pdp ");
		sql.append("  where pdp.status = 1 ");
		if (cpf != null) {
			sql.append("    and pdp.cpf = ? ");
		}

		if (!nome.isEmpty()) {
			sql.append("    and pdp.nome = ? ");
		}

		sql.append(" and pdp.data_bloqueio = (select max(pdp2.data_bloqueio) ");
		sql.append(" from DBCRED.TB_PROTECAO_DADOS_PESSOAIS pdp2 ");
		sql.append(" where pdp2.cpf = pdp.cpf)  ");
		ResultSet resultSet = null;
		PreparedStatement preparedStatement = null;
		Connection conn2 = this.getConnection();

		try {
//            preparedStatement = conn.prepareStatement ("SELECT * from DBCRED.TB_PROTECAO_DADOS_PESSOAIS WHERE cpf = ?");

			preparedStatement = conn2.prepareStatement(sql.toString());
			preparedStatement.setString(1, cpf);

			if (!nome.isEmpty()) {
				preparedStatement.setString(2, nome);
			}
			resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
				return true;
			}

		} catch (SQLException e) {
			logger.error("Erro no metodo verificaProtecaoDadosPessoais da classe Resposta: " + e.getMessage());
			return false;
		} finally {
			/*
			 * preparedStatement.close(); resultSet.close(); conn2.close();
			 */

			if (resultSet != null && !resultSet.isClosed())
				resultSet.close();
			if (preparedStatement != null && !preparedStatement.isClosed())
				preparedStatement.close();
		}

		return false;
	}

	public void fechaConexao() throws SQLException {
		if (this.connection != null)
			this.connection.close();
	}

}