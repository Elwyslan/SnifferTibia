import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class Main {
	public static int SAMPLE_STEP_TIME = 10;//Tempo entre cada amostragem em minutos
	public static StringBuffer log;
	public static FileWriter fileLog = null;
	public static PrintWriter writeLog = null;
	

	public static void main(String[] args) {
		String[] worlds = {"Julera", "Dolera","Tenxebra", "Luminera", "Inferna", "Neptera"};//teste
		
		//args = worlds;	/* Remova o comentario desta linha para testar o código*/
		
		if(args.length != 0)
			worlds = args;
		else{
			System.out.println("Defina um ou mais mundos para pesquisar e execute novamente o programa!");
			System.out.println("Sugestão: java -jar snifferWorld.jar Luminera Premia Julera");
			System.out.println("Para mais mundos acesse: https://secure.tibia.com/community/?subtopic=worlds");
			System.out.println("FIM!");
			System.exit(0);
		}
		
		//Cria o arquivo para armazenar o log
		createFileLog(args);
		
		double[] means = new double[worlds.length];
		int[] qtdSamples = new int[worlds.length];
		log = new StringBuffer();
		
		java.util.Date date = new java.util.Date();
		System.out.print("Inicio do programa: ");
	    System.out.println(date);
	    System.out.print("\n");
		
	    int countTime = 0;
		while(countTime < 144){
			//Pega a tabela que contem as informações, de todos os mundos, da URL "https://secure.tibia.com/community/?subtopic=worlds"
			Document doc;
			Elements worldTable = null;
			try{
				doc = Jsoup.connect("https://secure.tibia.com/community/?subtopic=worlds").get();
				worldTable = doc.getElementsByClass("TableContentContainer");
			}catch(Exception e){e.printStackTrace(); System.exit(0);}
			
			//Extrai o html que gerou a tabela dos mundos
			String html = worldTable.toString();
			
			//Armazena no log a data e horário
			date = new java.util.Date();
			log.append(date.toString()+"\n");
			
			//Extrai a quantidade de players em cada um dos mundos
			for(int i=0; i<worlds.length; i++){
				int qtd_OnlinePlayer = getNumberOfPlayers(html, worlds[i]);
				
				if(qtd_OnlinePlayer == -1){
					System.out.println(worlds[i]+": ERRO  *****  Média: ERRO");
					log.append(worlds[i]+": ERRO  *****  Média: ERRO\n");
				}
				else{
					means[i] += (double)qtd_OnlinePlayer;
					qtdSamples[i]++;
					System.out.println(worlds[i]+": "+qtd_OnlinePlayer+" players  *****  Média:"+means[i]/qtdSamples[i]);
					log.append(worlds[i]+": "+qtd_OnlinePlayer+" players  *****  Média:"+means[i]/qtdSamples[i]+"\n");
				}
			}
			log.append("\n");
			
			//Grava os dados do log no arquivo txt
			writeLogInfo();
			
			//Espera 10min para realizar a próxima consulta.
			waitNextSample();
			
			countTime++;
		}
		
		System.out.println("Fim!");
	}

	private static int getNumberOfPlayers(String html, String world){
		int n;
		
		String findURL = "https://secure.tibia.com/community/?subtopic=worlds&amp;world="+world;
		
		n = html.indexOf(findURL);
		
		if(n==-1)
			return -1;
		
		String s = html.substring(n, n + findURL.length() + 60);
		//System.out.println(s);
		
		n = s.indexOf("<td>");
		
		s = s.substring(n+4, n+7);
		
		if(s.endsWith("<")){
			s = s.substring(0, 2);
			return Integer.parseInt(s);
		}else{
			try{
				int ret = Integer.parseInt(s);
				return ret;
			}catch(Exception e){
				return -1;
			}
		}
	}
	
	private static void waitNextSample(){
		java.util.Date date;
		int t = 60 * SAMPLE_STEP_TIME;//Tempo em segundos
		
		System.out.print("\n0s ");
		for(int i=0; i<t; i++){
			try {TimeUnit.SECONDS.sleep(1);}catch(InterruptedException e){e.printStackTrace();}
			
			if(i%2==0)
				System.out.print(".");
			
			if((i+1)%60==0){
				date = new java.util.Date();
				System.out.print(" "+i+"s - "+date+"\n"+(i+1)+"s ");
			}
		}
		System.out.print("\n");
		date = new java.util.Date();
	    System.out.println(date);
	    System.out.print("\n");
	}
	
	
	
	private static void createFileLog(String[] worldNames){
		//Gerar o nome do arquivo
		String name = "log_MediaDePlayers_";
		for(int i=0; i<worldNames.length; i++){
			name = name + worldNames[i] + "_";
		}
		name = name.substring(0, name.length()-1);
		name += ".txt";
		
		//Cria o arquivo de texto
		try{fileLog = new FileWriter(name, true);}catch (IOException e){e.printStackTrace(); System.exit(0);}
		
		//Criar o "printer"
		writeLog = new PrintWriter(fileLog);
	}
	
	private static void writeLogInfo() {
		writeLog.printf(log.toString().replaceAll("\n", System.lineSeparator()));
		writeLog.flush();
		log.setLength(0);
	}
	
	
}
