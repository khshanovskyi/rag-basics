# RAG (Retrival Arguments Generation): 

## Pre Steps:
1. 🔑 Add to the project Open-AI API key https://platform.openai.com/settings/organization/api-keys (It is not Free, you
   need to pay 1-10$ for subscription)
2. Run [docker-compose](docker-compose.yml). It will run container with postgres + [pg vector extension](https://www.postgresql.org/about/news/pgvector-070-released-2852/) and creates the table from [this script](init-scripts/init.sql) 

## You need to implement:
1. Add Open AI keys in [Constant](src/main/java/task/utils/Constant.java) class
2. [ChatApp](src/main/java/task/ChatApp.java), implement all the flow described in `todo`
3. [OpenAIEmbeddingsClient](src/main/java/task/clients/OpenAIEmbeddingsClient.java), implement all the flow described in `todo`
4. [TextProcessor](src/main/java/task/documents/TextProcessor.java), implement all the flow described in `todo`


## Valid request samples:
``` 
What safety precautions should be taken to avoid exposure to excessive microwave energy?
```
```
What is the maximum cooking time that can be set on the DW 395 HCG microwave oven?
```
```
How should you clean the glass tray of the microwave oven?
```
```
What materials are safe to use in this microwave during both microwave and grill cooking modes?
```
```
What are the steps to set the clock time on the DW 395 HCG microwave oven?
```
```
What is the ECO function on this microwave and how do you activate it?
```
```
What are the specifications for proper installation, including the required free space around the oven?
```
```
How does the multi-stage cooking feature work, and what types of cooking programs cannot be included in it?
```
```
What should you do if food in plastic or paper containers starts smoking during heating?
```
```
What is the recommended procedure for removing odors from the microwave oven?
```

## Invalid request samples:
```
What do you know about the Codeus community?
```
```
What do you think about the dinosaur era? Why did they die?
```