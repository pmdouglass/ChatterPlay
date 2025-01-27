package com.example.chatterplay.repository


/*
Repository


fun saveMysterCallerPairs(crRoomId: String, gameName: String, pairs: List<Pair<String, String>>, onSuccess: () -> Unit, onError: (Exception) -> Unit){
        val mysterCallerData = pairs.mapIndexed { index, pair ->
            "pair${index + 1}" to mapOf(
                "askerId" to pair.first,
                "hasAsked" to false,
                "recieverId" to pair.second,
                "hasAnswered" to false
            )
        }.toMap()

        val documentPath = crGameRoomsCollection
            .document(crRoomId)
            .collection("Games")
            .document(gameName)

        documentPath.set(mapOf("pairs" to mysterCallerData), SetOptions.merge())
            .addOnSuccessListener {exception ->
                Log.d("Repository", "Saved $gameName pairs successfullly.")
                onSuccess
            }
            .addOnFailureListener { exception ->
                Log.e("Repository", "Failed to save $gameName pairs: ${exception.message}")
                onError(exception)
            }
    }
    fun updateHasAsked(crRoomId: String, gameName: String, userId: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        val documentPath = crGameRoomsCollection
            .document(crRoomId)
            .collection("Games")
            .document(gameName)

        documentPath.get()
            .addOnSuccessListener { document ->
                try {
                    if (document.exists()) {
                        val pairsData = document.get("pairs") as? Map<String, Map<String, Any>>
                        if (pairsData != null) {
                            // Find the pair where `askerId` matches the userId
                            val updatedPairs = pairsData.mapValues { entry ->
                                if (entry.value["askerId"] == userId) {
                                    entry.value.toMutableMap().apply {
                                        this["hasAsked"] = true
                                    }
                                } else {
                                    entry.value
                                }
                            }

                            // Update the document with the modified pairs
                            documentPath.update("pairs", updatedPairs)
                                .addOnSuccessListener {
                                    Log.d("Repository", "Updated hasAsked for user $userId successfully.")
                                    onSuccess()
                                }
                                .addOnFailureListener { exception ->
                                    Log.e("Repository", "Failed to update hasAsked for user $userId: ${exception.message}")
                                    onError(exception)
                                }
                        } else {
                            Log.d("Repository", "No pairs data found.")
                            onSuccess()
                        }
                    } else {
                        Log.d("Repository", "No document found for $gameName.")
                        onSuccess()                    }
                } catch (e: Exception) {
                    Log.e("Repository", "Error processing $gameName data: ${e.message}")
                    onError(e)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Repository", "Failed to fetch $gameName data: ${exception.message}")
                onError(exception)
            }
    }
    fun checkIfUserHasAnswered(crRoomId: String, gameName: String, userId: String, toYou: Boolean, onComplete: (Boolean) -> Unit, onError: (Exception) -> Unit) {
        val documentPath = crGameRoomsCollection
            .document(crRoomId)
            .collection("Games")
            .document(gameName)

        documentPath.get()
            .addOnSuccessListener { document ->
                try {
                    if (document.exists()) {
                        val pairsData = document.get("pairs") as? Map<String, Map<String, Any>>
                        if (pairsData != null) {
                            val hasAnswered = pairsData.any { (_, pairData) ->
                                val askerId = pairData["askerId"] as? String
                                val receiverId = pairData["receiverId"] as? String
                                val answered = pairData["hasAnswered"] as? Boolean ?: false

                                if (toYou) {
                                    receiverId == userId && answered
                                } else {
                                    askerId == userId && answered
                                }
                            }
                            Log.d("Repository", "Check hasAnswered result: $hasAnswered")
                            onComplete(hasAnswered)
                        } else {
                            Log.d("Repository", "No pairs data found.")
                            onComplete(false)
                        }
                    } else {
                        Log.d("Repository", "No document found for $gameName")
                        onComplete(false)
                    }
                } catch (e: Exception) {
                    Log.e("Repository", "Error processing $gameName data: ${e.message}")
                    onError(e)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Repository", "Failed to check hasAnswered: ${exception.message}")
                onError(exception)
            }
    }
    fun fetchMysteryCallerPairs(crRoomId: String, gameName: String, onComplete: (List<Pair<String, String>>) -> Unit, onError: (Exception) -> Unit) {
        val documentPath = crGameRoomsCollection
            .document(crRoomId)
            .collection("Games")
            .document(gameName)

        documentPath.get()
            .addOnSuccessListener { document ->
                try {
                    if (document.exists()) {
                        val pairsData = document.get("pairs") as? Map<String, Map<String, String>>
                        if (pairsData != null){
                            val pairs = pairsData.mapNotNull {
                                val askerId = it.value["askerId"]
                                val recieverId = it.value["recieverId"]
                                if (askerId != null && recieverId != null){
                                    askerId to recieverId
                                }else {
                                    null
                                }
                            }
                            Log.d("Repository", "Fetched $gameName pairs successfully: $pairs")
                            onComplete(pairs)
                        }
                    }else {
                        Log.d("Repository", "No document found for $gameName")
                        onComplete(emptyList())
                    }
                }catch (e: Exception){
                    Log.e("Repository", "Error processing $gameName data: ${e.message}")
                    onError(e)
                }

            }
            .addOnFailureListener { exception ->
                Log.e("Repository", "Failed to fetch $gameName pairs: ${exception.message}")
                onError(exception)
            }
    }
    suspend fun getSelectedUserProfile(crRoomId: String, userId: String): UserProfile?{
        return try {
            val documentPath = crGameRoomsCollection
                .document(crRoomId)
                .collection("Users")
                .document(userId)
                .get().await()

            documentPath.toObject(UserProfile::class.java)
        }catch (e: Exception){
            Log.e("Repository", "Failed to fetch UserProfile for userId: $userId in room: $crRoomId")
            null
        }
    }
 */



/*

ViewModel



//               supabase anon questions
    private val _theyHaveAsked = MutableStateFlow(false)
    val theyHaveAsked: StateFlow<Boolean> = _theyHaveAsked.asStateFlow()
    private val _youHaveAsked = MutableStateFlow(false)
    val youHaveAsked: StateFlow<Boolean> = _youHaveAsked.asStateFlow()
    private val _theyHaveAnswered = MutableStateFlow(false)
    val theyHaveAnswered: StateFlow<Boolean> = _theyHaveAnswered.asStateFlow()
    private val _youHaveAnswered = MutableStateFlow(false)
    val youHaveAnswered: StateFlow<Boolean> = _youHaveAnswered.asStateFlow()

    fun saveTargetQuestion(crRoomId: String, title: String, question: askQuestion, userId: String, toUserId: String){
        viewModelScope.launch {
            try {
                client.postgrest["askQuestion"].insert(question)
                Log.d("ViewModel", "Saved 1 Question from $userId")

                chatRepository.updateHasAsked(
                    crRoomId = crRoomId,
                    gameName = title,
                    userId = userId,
                    onSuccess = {
                        fetchToUserQuestion(crRoomId, userId = userId, toUserId = toUserId)
                        Log.d("ViewModel", "Successfully updated hasAsked for user $userId")
                    },
                    onError = {exception ->
                        Log.e("ViewModel", "error updating hasAsked for user $userId: ${exception.message}")
                    }
                )

                //updateHasAnswered(crRoomId)
                //checkForUsersCompleteAnswers(crRoomId, gameInfo.title)
            }catch (e: Exception){
                Log.d("ViewModel", "Failed to save pair answers ${e.message}")
            }
        }
    }
    private val _yourAnonQuestion = mutableStateOf<askQuestion?>(null)
    val yourAnonQuestion: State<askQuestion?> = _yourAnonQuestion
    private val _yourQuestion= mutableStateOf<askQuestion?>(null)
    val yourQuestion: State<askQuestion?> = _yourQuestion
    fun fetchToUserQuestion(crRoomId: String, userId: String, toUserId: String){
        viewModelScope.launch {
            Log.d("ViewModel", "Launching fetchToUserQuestion")
            // you are targeting
            try {
                val response = client.postgrest["askQuestion"]
                    .select(
                        filter = {
                            filter("userId", FilterOperator.EQ, userId)
                            filter("toUserId", FilterOperator.EQ, toUserId)
                            filter("crRoomId", FilterOperator.EQ, crRoomId)
                        }
                    )
                    .decodeSingleOrNull<askQuestion>()

                _yourAnonQuestion.value = response
                Log.d("ViewModel", "Successfully got question: $response from $userId to $toUserId")
            }catch (e: Exception){
                Log.d("ViewModel", "Failed to check Users Question ${e.message}")
            }

        }
    }
    fun fetchTargetQuestion(crRoomId: String, userId: String){
        viewModelScope.launch {
            Log.d("ViewModel", "Launching fetchTargetQuestion")
            // you are targeted
            try {
                val response = client.postgrest["askQuestion"]
                    .select(
                        filter = {
                            filter("toUserId", FilterOperator.EQ, userId)
                            filter("crRoomId", FilterOperator.EQ, crRoomId)
                        }
                    )
                    .decodeSingleOrNull<askQuestion>()

                _yourQuestion.value = response
                Log.d("ViewModel", "Successfully got target question: $response for $userId")
            }catch (e: Exception){
                Log.d("ViewModel", "Failed to get Users targeted Anon Question ${e.message}")
            }
        }
    }
    fun createUserPairs(users: List<UserProfile>): List<Pair<String, String>>{
        val shuffledUsers = users.shuffled()
        val pairs = mutableListOf<Pair<String, String>>()

        for (i in shuffledUsers.indices){
            val currentUser = shuffledUsers[i].userId
            val nextUser = shuffledUsers[(i + 1) % shuffledUsers.size].userId
            pairs.add(currentUser to nextUser)
        }
        return pairs
    }
    fun saveMysterCallerPairs(
        crRoomId: String,
        gameName: String,
        pairs: List<Pair<String, String>>,
        onSuccess: () -> Unit
    ){
        chatRepository.saveMysterCallerPairs(
            crRoomId = crRoomId,
            gameName = gameName,
            pairs = pairs,
            onSuccess = onSuccess,
            onError = { exception ->
                Log.d("ViewModel", "Error saving Mystery Caller pairs: ${exception.message}")
            }
        )
    }
    private val _userPairs = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val userPairs: StateFlow<List<Pair<String, String>>> = _userPairs
    fun fetchMysteryCallerPairs(
        crRoomId: String,
        gameName: String
    ){
        chatRepository.fetchMysteryCallerPairs(
            crRoomId = crRoomId,
            onComplete = { pairs ->
                         _userPairs.value = pairs
                Log.d("ViewModel", "Fetched $gameName pairs successfully: $pairs")
            },
            gameName = gameName,
            onError = {exception ->
                Log.e("ViewModel", "Error fetchign Myster Caller pairs: ${exception.message}")
            }
        )
    }

    fun fetchUserProfile(crRoomId: String, userId: String, onComplete: (UserProfile?) -> Unit){
        viewModelScope.launch {
            val userProfile = chatRepository.getSelectedUserProfile(crRoomId, userId)
            onComplete(userProfile)
        }
    }

    fun saveAnonAnswer(question: Answers){
        viewModelScope.launch {
            try {
                client.postgrest["answers"].insert(question)

                Log.d("ViewModel", "Anonymous answer saved succesffylly: $question")
            }catch (e: Exception){
                Log.d("ViewModel", "Error saving anon answer ${e.message}")
            }
        }
    }
    private val _hadAnsweredAnonQuestion = mutableStateOf(false)
    val hasAnsweredAnonQuestion: State<Boolean> = _hadAnsweredAnonQuestion
    fun fetchAnonAnswer(crRoomId: String, question: String){
        viewModelScope.launch {
            try {
                val response = client.postgrest["answers"]
                    .select(
                        filter = {
                            filter("userId", FilterOperator.EQ, userId)
                            filter("crRoomId", FilterOperator.EQ, crRoomId)
                            filter("question", FilterOperator.EQ, question)
                        }
                    )
                    .decodeList<Answers>()

                _hadAnsweredAnonQuestion.value = response.isNotEmpty()

            }catch (e: Exception){
                Log.d("ViewModel", "Error fetching anon answer ${e.message}")
            }
        }
    }


    private val _QnAState = MutableStateFlow<String>("")
    val QnAState: StateFlow<String> = _QnAState


 */