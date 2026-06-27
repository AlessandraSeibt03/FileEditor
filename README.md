Leitor de Arquivos Binários
Projeto desenvolvido para a disciplina de Sistemas Operacionais.


O sistema permite abrir arquivos reais do computador, visualizar seu conteúdo em formato hexadecimal e ASCII, identificar o tipo do arquivo através de sua assinatura binária (Magic Numbers), calcular estatísticas, gerar hashes criptográficos e realizar edições controladas diretamente nos bytes do arquivo.


Objetivo:
Demonstrar conceitos estudados em Sistemas Operacionais relacionados à organização e representação de dados em arquivos binários.
O projeto foi desenvolvido utilizando apenas recursos nativos da linguagem Java, sem bibliotecas externas.


Funcionalidades:
- Abrir arquivos reais do computador
- Visualizar arquivos em hexadecimal e ASCII
- Navegar por setores do arquivo
- Editar bytes diretamente na tabela hexadecimal
- Buscar padrões em texto ou hexadecimal
- Exibir estatísticas do arquivo
- Gerar Hash MD5
- Gerar Hash SHA-1
- Gerar Hash SHA-256
- Detectar o tipo do arquivo utilizando Magic Numbers
- Comparar arquivos binários
- Trabalhar diretamente com arquivos reais do sistema
  

Tecnologias :
- Java
- Java Swing
- RandomAccessFile
- MessageDigest
- JFileChooser
Todas as funcionalidades foram implementadas utilizando apenas bibliotecas padrão do Java.


Estrutura do Projeto:

src/

│

├── Main.java

├── DiskEditorUI.java

├── DiskEditor.java

├── BinaryDiff.java

├── FileStatistics.java

├── HashCalculator.java

├── HexFormatter.java

├── MagicNumberDetector.java

└── MBRParser.java


Interface:
A aplicação possui interface gráfica desenvolvida com Java Swing.

Nela é possível:
- Abrir arquivos do computador;
- Navegar pelos setores;
- Visualizar bytes em hexadecimal;
- Visualizar representação ASCII;
- Editar bytes específicos;
- Consultar informações do arquivo.
  

Conceitos de Sistemas Operacionais Utilizados:

Durante o desenvolvimento foram aplicados diversos conceitos da disciplina, como:
- Organização de arquivos
- Manipulação binária
- Endereçamento por bytes
- Leitura por setores
- Estruturas de armazenamento
- Integridade de arquivos
- Assinaturas binárias (Magic Numbers)
- Funções Hash
- Acesso aleatório utilizando RandomAccessFile
  

Como executar:
Clone o repositório: https://github.com/AlessandraSeibt03/FileEditor

Abra o projeto no Visual Studio Code ou IntelliJ.
Execute a classe Main.java

Após abrir um arquivo na interface, o sistema apresenta:
- Offset
- Bytes em Hexadecimal
- Representação ASCII
- Informações do arquivo
- Estatísticas
- Hashes
- Tipo detectado
