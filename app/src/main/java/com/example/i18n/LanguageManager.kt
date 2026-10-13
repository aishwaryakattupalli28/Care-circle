package com.example.i18n

import java.util.Locale

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String, val locale: Locale) {
    ENGLISH("en", "English", "English", Locale("en", "US")),
    HINDI("hi", "Hindi", "हिन्दी", Locale("hi", "IN")),
    TELUGU("te", "Telugu", "తెలుగు", Locale("te", "IN")),
    TAMIL("ta", "Tamil", "தமிழ்", Locale("ta", "IN")),
    BENGALI("bn", "Bengali", "বাংলা", Locale("bn", "IN")),
    MARATHI("mr", "Marathi", "मराठी", Locale("mr", "IN")),
    GUJARATI("gu", "Gujarati", "ગુજરાતી", Locale("gu", "IN")),
    KANNADA("kn", "Kannada", "ಕನ್ನಡ", Locale("kn", "IN")),
    MALAYALAM("ml", "Malayalam", "മലയാളം", Locale("ml", "IN"))
}

object AppStrings {
    fun get(key: String, lang: AppLanguage): String {
        return translations[key]?.get(lang) ?: translations[key]?.get(AppLanguage.ENGLISH) ?: key
    }

    private val translations = mapOf(
        "app_title" to mapOf(
            AppLanguage.ENGLISH to "CareCircle",
            AppLanguage.HINDI to "केयरसर्कल",
            AppLanguage.TELUGU to "కేర్ సర్కిల్",
            AppLanguage.TAMIL to "கேர்சர்க்கிள்",
            AppLanguage.BENGALI to "কেয়ারসার্কেল",
            AppLanguage.MARATHI to "केअरसर्कल",
            AppLanguage.GUJARATI to "કેરસર્કલ",
            AppLanguage.KANNADA to "ಕೇರ್‌ಸರ್ಕಲ್",
            AppLanguage.MALAYALAM to "കെയർസർക്കിൾ"
        ),
        "tagline" to mapOf(
            AppLanguage.ENGLISH to "Vital Health & Family Care",
            AppLanguage.HINDI to "महत्वपूर्ण स्वास्थ्य एवं परिवार की देखभाल",
            AppLanguage.TELUGU to "ప్రాణ సమాన ఆరోగ్యం & కుటుంబ సంరక్షణ",
            AppLanguage.TAMIL to "உயிர் நலம் & குடும்ப பராமரிப்பு",
            AppLanguage.BENGALI to "গুরুত্বপূর্ণ স্বাস্থ্য ও পারিবারিক যত্ন",
            AppLanguage.MARATHI to "महत्त्वाचे आरोग्य आणि कुटुंबाची काळजी",
            AppLanguage.GUJARATI to "મહત્વપૂર્ણ આરોગ્ય અને પરિવાર સંભાળ",
            AppLanguage.KANNADA to "ಆರೋಗ್ಯ ರಕ್ಷಣೆ & ಕುಟುಂಬ ಆರೈಕೆ",
            AppLanguage.MALAYALAM to "ജീവൻരക്ഷാ ആരോഗ്യവും കുടുംബ പരിചരണവും"
        ),
        "nav_dashboard" to mapOf(
            AppLanguage.ENGLISH to "Vitals",
            AppLanguage.HINDI to "स्वास्थ्य",
            AppLanguage.TELUGU to "ఆరోగ్యం",
            AppLanguage.TAMIL to "ஆரோக்கியம்",
            AppLanguage.BENGALI to "স্বাস্থ্য",
            AppLanguage.MARATHI to "आरोग्य",
            AppLanguage.GUJARATI to "આરોગ્ય",
            AppLanguage.KANNADA to "ಆರೋಗ್ಯ",
            AppLanguage.MALAYALAM to "ആരോഗ്യം"
        ),
        "nav_simulator" to mapOf(
            AppLanguage.ENGLISH to "Simulator",
            AppLanguage.HINDI to "सिम्युलेटर",
            AppLanguage.TELUGU to "సిమ్యులేటర్",
            AppLanguage.TAMIL to "சிமுலேட்டர்",
            AppLanguage.BENGALI to "সিমুলেটর",
            AppLanguage.MARATHI to "सिम्युलेटर",
            AppLanguage.GUJARATI to "સિમ્યુલેટર",
            AppLanguage.KANNADA to "ಸಿಮ್ಯುಲೇಟರ್",
            AppLanguage.MALAYALAM to "സിമുലേറ്റർ"
        ),
        "nav_dose" to mapOf(
            AppLanguage.ENGLISH to "Medicines",
            AppLanguage.HINDI to "दवाइयां",
            AppLanguage.TELUGU to "మందులు",
            AppLanguage.TAMIL to "மருந்துகள்",
            AppLanguage.BENGALI to "ওষুধ",
            AppLanguage.MARATHI to "औषधे",
            AppLanguage.GUJARATI to "દવાઓ",
            AppLanguage.KANNADA to "ಔಷಧಿಗಳು",
            AppLanguage.MALAYALAM to "മരുന്നുകൾ"
        ),
        "nav_sos" to mapOf(
            AppLanguage.ENGLISH to "Emergency SOS",
            AppLanguage.HINDI to "आपातकालीन SOS",
            AppLanguage.TELUGU to "అత్యవసర SOS",
            AppLanguage.TAMIL to "அவசர SOS",
            AppLanguage.BENGALI to "জরুরি SOS",
            AppLanguage.MARATHI to "तातडीचे SOS",
            AppLanguage.GUJARATI to "ઇમરજન્સી SOS",
            AppLanguage.KANNADA to "ತುರ್ತು SOS",
            AppLanguage.MALAYALAM to "അടിയന്തര SOS"
        ),
        "nav_thresholds" to mapOf(
            AppLanguage.ENGLISH to "Settings",
            AppLanguage.HINDI to "सेटिंग्स",
            AppLanguage.TELUGU to "సెట్టింగ్‌లు",
            AppLanguage.TAMIL to "அமைப்புகள்",
            AppLanguage.BENGALI to "সেটিংস",
            AppLanguage.MARATHI to "सेटिंग्ज",
            AppLanguage.GUJARATI to "સેટિંગ્સ",
            AppLanguage.KANNADA to "ಸೆಟ್ಟಿಂಗ್‌ಗಳು",
            AppLanguage.MALAYALAM to "ക്രമീകരണങ്ങൾ"
        ),
        "voice_assistant" to mapOf(
            AppLanguage.ENGLISH to "Voice Assistant",
            AppLanguage.HINDI to "आवाज़ सहायक",
            AppLanguage.TELUGU to "వాయిస్ అసిస్టెంట్",
            AppLanguage.TAMIL to "குரல் உதவியாளர்",
            AppLanguage.BENGALI to "ভয়েস সহকারী",
            AppLanguage.MARATHI to "व्हॉइस असिस्टंट",
            AppLanguage.GUJARATI to "વોઇસ સહાયક",
            AppLanguage.KANNADA to "ಧ್ವನಿ ಸಹಾಯಕ",
            AppLanguage.MALAYALAM to "വോയ്സ് അസിസ്റ്റന്റ്"
        ),
        "voice_read_vitals" to mapOf(
            AppLanguage.ENGLISH to "Read Vitals Aloud",
            AppLanguage.HINDI to "स्वास्थ्य स्थिति बोलकर सुनाएं",
            AppLanguage.TELUGU to "ఆరోగ్య స్థితిని వినిపించండి",
            AppLanguage.TAMIL to "உடல்நிலையை வாசித்துக் காட்டு",
            AppLanguage.BENGALI to "স্বাস্থ্য রিপোর্ট শুনুন",
            AppLanguage.MARATHI to "आरोग्याची माहिती ऐका",
            AppLanguage.GUJARATI to "આરોગ્ય સ્થિતિ સાંભળો",
            AppLanguage.KANNADA to "ಆರೋಗ್ಯ ಸ್ಥಿತಿಯನ್ನು ಕೇಳಿ",
            AppLanguage.MALAYALAM to "ആരോഗ്യസ്ഥിതി കേൾക്കുക"
        ),
        "voice_stop" to mapOf(
            AppLanguage.ENGLISH to "Stop Voice",
            AppLanguage.HINDI to "आवाज़ बंद करें",
            AppLanguage.TELUGU to "వాయిస్ ఆపండి",
            AppLanguage.TAMIL to "நிறுத்து",
            AppLanguage.BENGALI to "থামুন",
            AppLanguage.MARATHI to "थांबवा",
            AppLanguage.GUJARATI to "બંધ કરો",
            AppLanguage.KANNADA to "ನಿಲ್ಲಿಸಿ",
            AppLanguage.MALAYALAM to "നിർത്തുക"
        ),
        "patient_label" to mapOf(
            AppLanguage.ENGLISH to "Patient",
            AppLanguage.HINDI to "मरीज",
            AppLanguage.TELUGU to "రోగి",
            AppLanguage.TAMIL to "நோயாளி",
            AppLanguage.BENGALI to "রোগী",
            AppLanguage.MARATHI to "रुग्ण",
            AppLanguage.GUJARATI to "દર્દી",
            AppLanguage.KANNADA to "ರೋಗಿ",
            AppLanguage.MALAYALAM to "രോഗി"
        ),
        "caregiver_label" to mapOf(
            AppLanguage.ENGLISH to "Caregiver",
            AppLanguage.HINDI to "देखभालकर्ता",
            AppLanguage.TELUGU to "సంరక్షకుడు",
            AppLanguage.TAMIL to "பராமரிப்பாளர்",
            AppLanguage.BENGALI to "যত্নকারী",
            AppLanguage.MARATHI to "काळजीवाहक",
            AppLanguage.GUJARATI to "સંભાળ રાખનાર",
            AppLanguage.KANNADA to "ಆರೈಕೆದಾರ",
            AppLanguage.MALAYALAM to "പരിപാലകൻ"
        ),
        "status_online" to mapOf(
            AppLanguage.ENGLISH to "LIVE STREAMING",
            AppLanguage.HINDI to "लाइव टेलीमेट्री चालू",
            AppLanguage.TELUGU to "లైవ్ ప్రసారం",
            AppLanguage.TAMIL to "நேரலை ஸ்ட்ரீமிங்",
            AppLanguage.BENGALI to "লাইভ চলছে",
            AppLanguage.MARATHI to "थेट प्रवाह सुरू",
            AppLanguage.GUJARATI to "લાઇવ સ્ટ્રીમિંગ",
            AppLanguage.KANNADA to "ಲೈವ್ ಸ್ಟ್ರೀಮಿಂಗ್",
            AppLanguage.MALAYALAM to "തത്സമയ സ്ട്രീമിംഗ്"
        ),
        "status_offline" to mapOf(
            AppLanguage.ENGLISH to "DISCONNECTED",
            AppLanguage.HINDI to "कनेक्शन कटा हुआ",
            AppLanguage.TELUGU to "కనెక్షన్ తెగిపోయింది",
            AppLanguage.TAMIL to "இணைப்பு துண்டிக்கப்பட்டது",
            AppLanguage.BENGALI to "সংযোগ বিচ্ছিন্ন",
            AppLanguage.MARATHI to "संपर्क तुटला",
            AppLanguage.GUJARATI to "કનેક્શન કપાઈ ગયું",
            AppLanguage.KANNADA to "ಸಂಪರ್ಕ ಕಡಿತಗೊಂಡಿದೆ",
            AppLanguage.MALAYALAM to "ബന്ധം വിച്ഛേദിച്ചു"
        ),
        "heart_rate" to mapOf(
            AppLanguage.ENGLISH to "Heart Rate",
            AppLanguage.HINDI to "हृदय गति (Heart Rate)",
            AppLanguage.TELUGU to "గుండె కొట్టుకోవటం (Heart Rate)",
            AppLanguage.TAMIL to "இதயத் துடிப்பு",
            AppLanguage.BENGALI to "হৃদস্পন্দন",
            AppLanguage.MARATHI to "हृदयाचे ठोके",
            AppLanguage.GUJARATI to "હૃદયના ધબકારા",
            AppLanguage.KANNADA to "ಹೃದಯ ಬಡಿತ",
            AppLanguage.MALAYALAM to "ഹൃദയമിടിപ്പ്"
        ),
        "spo2" to mapOf(
            AppLanguage.ENGLISH to "Blood Oxygen (SpO2)",
            AppLanguage.HINDI to "ऑक्सीजन स्तर (SpO2)",
            AppLanguage.TELUGU to "ఆక్సిజన్ స్థాయి (SpO2)",
            AppLanguage.TAMIL to "இரத்த ஆக்ஸிஜன் (SpO2)",
            AppLanguage.BENGALI to "রক্তে অক্সিজেন (SpO2)",
            AppLanguage.MARATHI to "रक्तातील ऑक्सिजन (SpO2)",
            AppLanguage.GUJARATI to "રક્ત ઓક્સિજન (SpO2)",
            AppLanguage.KANNADA to "ರಕ್ತದ ಆಮ್ಲಜನಕ (SpO2)",
            AppLanguage.MALAYALAM to "രക്തത്തിലെ ഓക്സിജൻ (SpO2)"
        ),
        "temperature" to mapOf(
            AppLanguage.ENGLISH to "Body Temperature",
            AppLanguage.HINDI to "शरीर का तापमान",
            AppLanguage.TELUGU to "శరీర ఉష్ణోగ్రత",
            AppLanguage.TAMIL to "உடல் வெப்பநிலை",
            AppLanguage.BENGALI to "শরীরের তাপমাত্রা",
            AppLanguage.MARATHI to "शरीराचे तापमान",
            AppLanguage.GUJARATI to "શરીરનું તાપમાન",
            AppLanguage.KANNADA to "ದೇಹದ ತಾಪಮಾನ",
            AppLanguage.MALAYALAM to "ശരീര താപനില"
        ),
        "blood_pressure_sys" to mapOf(
            AppLanguage.ENGLISH to "Blood Pressure (Sys)",
            AppLanguage.HINDI to "रक्तचाप सिस्टोलिक (BP)",
            AppLanguage.TELUGU to "రక్తపోటు సిస్టోలిక్ (BP)",
            AppLanguage.TAMIL to "இரத்த அழுத்தம் (சிஸ்டாலிக்)",
            AppLanguage.BENGALI to "রক্তচাপ (সিস্টোলিক)",
            AppLanguage.MARATHI to "रक्तदाब (सिस्टोलिक)",
            AppLanguage.GUJARATI to "બ્લડ પ્રેશર (સિસ્ટોલિક)",
            AppLanguage.KANNADA to "ರಕ್ತದೊತ್ತಡ (ಸಿಸ್ಟೋಲಿಕ್)",
            AppLanguage.MALAYALAM to "രക്തസമ്മർദ്ദം (സിസ്റ്റോളിക്)"
        ),
        "blood_pressure_dia" to mapOf(
            AppLanguage.ENGLISH to "Blood Pressure (Dia)",
            AppLanguage.HINDI to "रक्तचाप डायस्टोलिक (BP)",
            AppLanguage.TELUGU to "రక్తపోటు డయాస్టోలిక్ (BP)",
            AppLanguage.TAMIL to "இரத்த அழுத்தம் (டயஸ்டாலிக்)",
            AppLanguage.BENGALI to "রক্তচাপ (ডায়াস্টোলিক)",
            AppLanguage.MARATHI to "रक्तदाब (डायस्टोलिक)",
            AppLanguage.GUJARATI to "બ્લડ પ્રેશર (ડાયસ્ટોલિક)",
            AppLanguage.KANNADA to "ರಕ್ತದೊತ್ತಡ (ಡಯಾಸ್ಟೋಲಿಕ್)",
            AppLanguage.MALAYALAM to "രക്തസമ്മർദ്ദം (ഡയസ്റ്റോളിക്)"
        ),
        "respiratory_rate" to mapOf(
            AppLanguage.ENGLISH to "Breathing Rate",
            AppLanguage.HINDI to "श्वसन दर (Breathing)",
            AppLanguage.TELUGU to "శ్వాస రేటు (Breathing)",
            AppLanguage.TAMIL to "சுவாச விகிதம்",
            AppLanguage.BENGALI to "শ্বাসপ্রশ্বাসের হার",
            AppLanguage.MARATHI to "श्वसन दर",
            AppLanguage.GUJARATI to "શ્વસન દર",
            AppLanguage.KANNADA to "ಉಸಿರಾಟದ ದರ",
            AppLanguage.MALAYALAM to "ശ്വാസോച്ഛ്വാസ നിരക്ക്"
        ),
        "fresh_live" to mapOf(
            AppLanguage.ENGLISH to "LIVE",
            AppLanguage.HINDI to "लाइव",
            AppLanguage.TELUGU to "లైవ్",
            AppLanguage.TAMIL to "நேரலை",
            AppLanguage.BENGALI to "লাইভ",
            AppLanguage.MARATHI to "थेट",
            AppLanguage.GUJARATI to "લાઇવ",
            AppLanguage.KANNADA to "ಲೈವ್",
            AppLanguage.MALAYALAM to "തത്സമയം"
        ),
        "fresh_delayed" to mapOf(
            AppLanguage.ENGLISH to "DELAYED",
            AppLanguage.HINDI to "विलंबित",
            AppLanguage.TELUGU to "ఆలస్యమైనది",
            AppLanguage.TAMIL to "தாமதமானது",
            AppLanguage.BENGALI to "বিলম্বিত",
            AppLanguage.MARATHI to "उशिरा",
            AppLanguage.GUJARATI to "વિલંબિત",
            AppLanguage.KANNADA to "ವಿಳಂಬವಾಗಿದೆ",
            AppLanguage.MALAYALAM to "വൈകിയ"
        ),
        "fresh_stale" to mapOf(
            AppLanguage.ENGLISH to "STALE",
            AppLanguage.HINDI to "पुराना डेटा",
            AppLanguage.TELUGU to "పాత డేటా",
            AppLanguage.TAMIL to "பழையது",
            AppLanguage.BENGALI to "পুরোনো",
            AppLanguage.MARATHI to "जुने",
            AppLanguage.GUJARATI to "જૂનો ડેટા",
            AppLanguage.KANNADA to "ಹಳೆಯ ಡೇಟಾ",
            AppLanguage.MALAYALAM to "പഴയ ഡാറ്റ"
        ),
        "sos_title" to mapOf(
            AppLanguage.ENGLISH to "Emergency SOS",
            AppLanguage.HINDI to "आपातकालीन मदद (SOS)",
            AppLanguage.TELUGU to "అత్యవసర సహాయం (SOS)",
            AppLanguage.TAMIL to "அவசர உதவி (SOS)",
            AppLanguage.BENGALI to "জরুরি সহায়তা (SOS)",
            AppLanguage.MARATHI to "तातडीची मदत (SOS)",
            AppLanguage.GUJARATI to "ઇમરજન્સી મદદ (SOS)",
            AppLanguage.KANNADA to "ತುರ್ತು ಸಹಾಯ (SOS)",
            AppLanguage.MALAYALAM to "അടിയന്തര സഹായം (SOS)"
        ),
        "sos_press" to mapOf(
            AppLanguage.ENGLISH to "PRESS FOR HELP",
            AppLanguage.HINDI to "मदद के लिए दबाएं",
            AppLanguage.TELUGU to "సహాయం కోసం నొక్కండి",
            AppLanguage.TAMIL to "உதவிக்கு அழுத்தவும்",
            AppLanguage.BENGALI to "সাহায্যের জন্য চাপুন",
            AppLanguage.MARATHI to "मदतीसाठी दाबा",
            AppLanguage.GUJARATI to "મદદ માટે દબાવો",
            AppLanguage.KANNADA to "ಸಹಾಯಕ್ಕಾಗಿ ಒತ್ತಿರಿ",
            AppLanguage.MALAYALAM to "സഹായത്തിനായി അമർത്തുക"
        ),
        "sos_cancel" to mapOf(
            AppLanguage.ENGLISH to "Cancel Emergency Call",
            AppLanguage.HINDI to "आपातकालीन कॉल रद्द करें",
            AppLanguage.TELUGU to "అత్యవసర కాల్ రద్దు చేయండి",
            AppLanguage.TAMIL to "அவசர அழைப்பை ரத்துசெய்",
            AppLanguage.BENGALI to "জরুরি কল বাতিল করুন",
            AppLanguage.MARATHI to "कॉल रद्द करा",
            AppLanguage.GUJARATI to "કોલ રદ કરો",
            AppLanguage.KANNADA to "ಕರೆ ರದ್ದುಮಾಡಿ",
            AppLanguage.MALAYALAM to "കോൾ റദ്ദാക്കുക"
        ),
        "dose_title" to mapOf(
            AppLanguage.ENGLISH to "Medication & Dose Schedule",
            AppLanguage.HINDI to "दवाई और खुराक का समय",
            AppLanguage.TELUGU to "మందులు & మోతాదు సమయం",
            AppLanguage.TAMIL to "மருந்து & டோஸ் அட்டவணை",
            AppLanguage.BENGALI to "ওষুধ ও ডোজের সময়সূচী",
            AppLanguage.MARATHI to "औषध आणि डोसचे वेळापत्रक",
            AppLanguage.GUJARATI to "દવા અને ડોઝનું સમયપત્રક",
            AppLanguage.KANNADA to "ಔಷಧಿ ಮತ್ತು ಡೋಸ್ ವೇಳಾಪಟ್ಟಿ",
            AppLanguage.MALAYALAM to "മരുന്നും ഡോസ് സമയക്രമവും"
        ),
        "dose_take" to mapOf(
            AppLanguage.ENGLISH to "Mark as Taken",
            AppLanguage.HINDI to "दवाई ले ली गई",
            AppLanguage.TELUGU to "తీసుకున్నట్లు గుర్తించండి",
            AppLanguage.TAMIL to "எடுத்துக்கொண்டதாக குறிக்கவும்",
            AppLanguage.BENGALI to "ঔষধ গ্রহণ সম্পন্ন",
            AppLanguage.MARATHI to "घेतल्याची नोंद करा",
            AppLanguage.GUJARATI to "લીધેલ તરીકે ચિહ્નિત કરો",
            AppLanguage.KANNADA to "ತೆಗೆದುಕೊಂಡಂತೆ ಗುರುತಿಸಿ",
            AppLanguage.MALAYALAM to "കഴിച്ചതായി രേഖപ്പെടുത്തുക"
        ),
        "sim_title" to mapOf(
            AppLanguage.ENGLISH to "Live Clinical Scenario Simulator",
            AppLanguage.HINDI to "क्लिनिकल स्थिति सिम्युलेटर",
            AppLanguage.TELUGU to "క్లినికల్ దృశ్యాల సిమ్యులేటర్",
            AppLanguage.TAMIL to "மருத்துவ சூழல் சிமுலேட்டர்",
            AppLanguage.BENGALI to "ক্লিনিকাল পরিস্থিতি সিমুলেটর",
            AppLanguage.MARATHI to "वैद्यकीय परिस्थिती सिम्युलेटर",
            AppLanguage.GUJARATI to "ક્લિનિકલ સ્થિતિ સિમ્યુલેટર",
            AppLanguage.KANNADA to "ಕ್ಲಿನಿಕಲ್ ಸನ್ನಿವೇಶ ಸಿಮ್ಯುಲೇಟರ್",
            AppLanguage.MALAYALAM to "ക്ലിനിക്കൽ സിമുലേറ്റർ"
        ),
        "sim_desc" to mapOf(
            AppLanguage.ENGLISH to "Trigger abnormal medical conditions to test the rules engine, live freshness badges, and caregiver escalation ladder.",
            AppLanguage.HINDI to "नियम इंजन, लाइव ताजगी बैज और देखभालकर्ता सूचना सीढ़ी का परीक्षण करने के लिए चिकित्सा स्थितियों को सक्रिय करें।",
            AppLanguage.TELUGU to "రూల్స్ ఇంజిన్, లైవ్ ఫ్రెష్‌నెస్ బ్యాడ్జ్‌లు మరియు సంరక్షకుల అలర్ట్ నిచ్చెనను పరీక్షించడానికి వైద్య దృశ్యాలను ప్రారంభించండి.",
            AppLanguage.TAMIL to "விதிகள் எஞ்சின், நேரலை நிலை மற்றும் எச்சரிக்கை அமைப்பை சோதிக்க மருத்துவ சூழல்களைத் தூண்டவும்.",
            AppLanguage.BENGALI to "নিয়ম ইঞ্জিন, লাইভ ফ্রেশনেস এবং সতর্কতা ব্যবস্থা পরীক্ষা করতে চিকিৎসা পরিস্থিতি শুরু করুন।",
            AppLanguage.MARATHI to "नियम इंजिन, थेट स्थिती आणि काळजीवाहक अलर्ट तपासण्यासाठी वैद्यकीय परिस्थिती सुरू करा.",
            AppLanguage.GUJARATI to "નિયમોનું એન્જિન, લાઇવ સ્થિતિ અને ચેતવણી સિસ્ટમ ચકાસવા માટે તબીબી સ્થિતિઓ શરૂ કરો.",
            AppLanguage.KANNADA to "ನಿಯಮಗಳ ಎಂಜಿನ್ ಮತ್ತು ಎಚ್ಚರಿಕೆಯ ವ್ಯವಸ್ಥೆಯನ್ನು ಪರೀಕ್ಷಿಸಲು ವೈದ್ಯಕೀಯ ಸನ್ನಿವೇಶಗಳನ್ನು ಪ್ರಚೋದಿಸಿ.",
            AppLanguage.MALAYALAM to "നിയമങ്ങൾ എഞ്ചിനും മുന്നറിയിപ്പ് സംവിധാനവും പരിശോധിക്കാൻ അവസ്ഥകൾ ആരംഭിക്കുക."
        ),
        "threshold_title" to mapOf(
            AppLanguage.ENGLISH to "Safety Thresholds & Alert Limits",
            AppLanguage.HINDI to "सुरक्षा सीमाएं और अलर्ट नियम",
            AppLanguage.TELUGU to "భద్రతా పరిమితులు & అలర్ట్ నిబంధనలు",
            AppLanguage.TAMIL to "பாதுகாப்பு வரம்புகள் & விழிப்பூட்டல்கள்",
            AppLanguage.BENGALI to "সুরক্ষা সীমা ও সতর্কতা নিয়ম",
            AppLanguage.MARATHI to "सुरक्षा मर्यादा आणि नियम",
            AppLanguage.GUJARATI to "સુરક્ષા મર્યાદા અને ચેતવણીઓ",
            AppLanguage.KANNADA to "ಸುರಕ್ಷತಾ ಮಿತಿಗಳು & ಎಚ್ಚರಿಕೆಗಳು",
            AppLanguage.MALAYALAM to "സുരക്ഷാ പരിധികളും മുന്നറിയിപ്പുകളും"
        ),
        "select_language" to mapOf(
            AppLanguage.ENGLISH to "Change App Language",
            AppLanguage.HINDI to "ऐप की भाषा बदलें",
            AppLanguage.TELUGU to "యాప్ భాషను మార్చండి",
            AppLanguage.TAMIL to "மொழியை மாற்றவும்",
            AppLanguage.BENGALI to "অ্যাপের ভাষা পরিবর্তন করুন",
            AppLanguage.MARATHI to "अ‍ॅपची भाषा बदला",
            AppLanguage.GUJARATI to "એપની ભાષા બદલો",
            AppLanguage.KANNADA to "ಅಪ್ಲಿಕೇಶನ್ ಭಾಷೆ ಬದಲಾಯಿಸಿ",
            AppLanguage.MALAYALAM to "ആപ്പ് ഭാഷ മാറ്റുക"
        )
    )

    fun getSpokenVitalsSummary(
        lang: AppLanguage,
        patientName: String,
        heartRate: Double,
        spo2: Double,
        temp: Double,
        bpSys: Double,
        bpDia: Double
    ): String {
        return when (lang) {
            AppLanguage.HINDI -> "नमस्ते। मरीज ${patientName} की स्वास्थ्य स्थिति इस प्रकार है। हृदय गति ${heartRate.toInt()} बीट प्रति मिनट है। ऑक्सीजन का स्तर ${spo2.toInt()} प्रतिशत है। शरीर का तापमान ${temp} डिग्री सेल्सियस है। और रक्तचाप ${bpSys.toInt()} और ${bpDia.toInt()} मिलीमीटर मरकरी है। सभी पैरामीटर सामान्य सीमा में हैं।"
            AppLanguage.TELUGU -> "నమస్కారం. రోగి ${patientName} ఆరోగ్య పరిస్థితి ఇలా ఉంది. గుండె వేగం నిమిషానికి ${heartRate.toInt()} బీట్లు. ఆక్సిజన్ స్థాయి ${spo2.toInt()} శాతం. శరీర ఉష్ణోగ్రత ${temp} డిగ్రీల సెల్సియస్. రక్తపోటు ${bpSys.toInt()} బై ${bpDia.toInt()}. ఆరోగ్యం స్థిరంగా ఉంది."
            AppLanguage.TAMIL -> "வணக்கம். நோயாளி ${patientName} அவர்களின் உடல்நல விவரம். இதயத் துடிப்பு நிமிடத்திற்கு ${heartRate.toInt()}. ஆக்சிஜன் அளவு ${spo2.toInt()} சதவீதம். உடல் வெப்பநிலை ${temp} டிகிரி செல்சியஸ். இரத்த அழுத்தம் ${bpSys.toInt()} மற்றும் ${bpDia.toInt()}. நிலைமை சீராக உள்ளது."
            AppLanguage.BENGALI -> "নমস্কার। রোগী ${patientName} এর বর্তমান স্বাস্থ্য প্রতিবেদন। হৃদস্পন্দন প্রতি মিনিটে ${heartRate.toInt()}। অক্সিজেনের মাত্রা ${spo2.toInt()} শতাংশ। শরীরের তাপমাত্রা ${temp} ডিগ্রি সেলসিয়াস। রক্তচাপ ${bpSys.toInt()} বাই ${bpDia.toInt()}। অবস্থা স্বাভাবিক।"
            AppLanguage.MARATHI -> "नमस्कार. रुग्ण ${patientName} यांची आरोग्य स्थिती. हृदयाचे ठोके प्रति मिनिट ${heartRate.toInt()}. ऑक्सिजन पातळी ${spo2.toInt()} टक्के. शरीराचे तापमान ${temp} अंश सेल्सिअस. रक्तदाब ${bpSys.toInt()} आणि ${bpDia.toInt()}. तब्येत ठीक आहे."
            AppLanguage.GUJARATI -> "નમસ્તે. દર્દી ${patientName} ની આરોગ્ય સ્થિતિ. હૃદયના ધબકારા ${heartRate.toInt()} પ્રતિ મિનિટ. ઓક્સિજન સ્તર ${spo2.toInt()} ટકા. શરીરનું તાપમાન ${temp} ડિગ્રી સેલ્સિયસ. બ્લડ પ્રેશર ${bpSys.toInt()} અને ${bpDia.toInt()}. સ્થિતિ સામાન્ય છે."
            AppLanguage.KANNADA -> "ನಮಸ್ಕಾರ. ರೋಗಿ ${patientName} ಅವರ ಆರೋಗ್ಯ ಸ್ಥಿತಿ. ಹೃದಯ ಬಡಿತ ನಿಮಿಷಕ್ಕೆ ${heartRate.toInt()}. ಆಮ್ಲಜನಕ ಮಟ್ಟ ${spo2.toInt()} ಪ್ರತಿಶತ. ದೇಹದ ತಾಪಮಾನ ${temp} ಡಿಗ್ರಿ ಸೆಲ್ಸಿಯಸ್. ರಕ್ತದೊತ್ತಡ ${bpSys.toInt()} ಮತ್ತು ${bpDia.toInt()}. ಸ್ಥಿತಿ ಸ್ಥಿರವಾಗಿದೆ."
            AppLanguage.MALAYALAM -> "നമസ്കാരം. രോഗി ${patientName} ന്റെ ആരോഗ്യസ്ഥിതി. ഹൃദയമിടിപ്പ് മിനിറ്റിൽ ${heartRate.toInt()}. ഓക്സിജൻ നില ${spo2.toInt()} ശതമാനം. ശരീര താപനില ${temp} ഡിഗ്രി സെൽഷ്യസ്. രക്തസമ്മർദ്ദം ${bpSys.toInt()} ബൈ ${bpDia.toInt()}. നില സുരക്ഷിതമാണ്."
            AppLanguage.ENGLISH -> "Hello. Here is the vital health summary for patient ${patientName}. Heart rate is ${heartRate.toInt()} beats per minute. Blood oxygen is ${spo2.toInt()} percent. Body temperature is ${temp} degrees Celsius. Blood pressure is ${bpSys.toInt()} over ${bpDia.toInt()} millimeters of mercury. Patient is stable."
        }
    }
}

// Extended translations for offline cache and nearby hospitals
fun getNearbyHospitalTranslations(lang: AppLanguage): Pair<String, String> {
    return when (lang) {
        AppLanguage.HINDI -> "निकटतम आपातकालीन अस्पताल (जीपीएस)" to "आपातकाल के समय तुरंत कॉल या नेविगेट करें"
        AppLanguage.TELUGU -> "సమీప అత్యవసర ఆసుపత్రులు (జీపీఎస్)" to "అత్యవసర సమయాల్లో వెంటనే కాల్ చేయండి లేదా నావిగేట్ చేయండి"
        AppLanguage.TAMIL -> "அருகிலுள்ள அவசர மருத்துவமனைகள்" to "அவசர காலத்தில் உடனடியாக அழைக்கவும் அல்லது செல்லவும்"
        AppLanguage.BENGALI -> "নিকটবর্তী জরুরি হাসপাতাল (জিপিএস)" to "জরুরি অবস্থায় অবিলম্বে কল করুন বা নেভিগেট করুন"
        AppLanguage.MARATHI -> "जवळची आपत्कालीन रुग्णालये" to "तातडीच्या वेळी त्वरित कॉल करा किंवा नेव्हिगेट करा"
        AppLanguage.GUJARATI -> "નજીકની ઇમરજન્સી હોસ્પિટલો" to "ઇમરજન્સીમાં તાત્કાલિક કોલ કરો અથવા નેવિગેટ કરો"
        AppLanguage.KANNADA -> "ಹತ್ತಿರದ ತುರ್ತು ಆಸ್ಪತ್ರೆಗಳು" to "ತುರ್ತು ಸಮಯದಲ್ಲಿ ತಕ್ಷಣ ಕರೆ ಮಾಡಿ ಅಥವಾ ನ್ಯಾವಿಗೇಟ್ ಮಾಡಿ"
        AppLanguage.MALAYALAM -> "അടുത്തുള്ള അത്യാഹിത ആശുപത്രികൾ" to "അടിയന്തര ഘട്ടങ്ങളിൽ ഉടൻ വിളിക്കുകയോ നാവിഗേറ്റ് ചെയ്യുകയോ ചെയ്യുക"
        AppLanguage.ENGLISH -> "Nearby Emergency Hospitals (GPS)" to "Call ambulance or get immediate navigation routes"
    }
}
