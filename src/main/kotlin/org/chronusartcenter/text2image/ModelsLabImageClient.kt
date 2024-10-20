package org.chronusartcenter.text2image

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.apache.logging.log4j.kotlin.logger
import java.io.IOException


class ModelsLabImageClient(private val apiKey: String) {
    val COMMUNITY_API_URL = "https://modelslab.com/api/v6/images/text2img"
    val REALTIME_API_URL = "https://modelslab.com/api/v6/realtime/text2img"

    enum class EnhanceType(val value: String) {
        ENHANCE("enhance"),
        CINEMATIC_DIVA("cinematic-diva"),
        NUDE("nude"),
        NSFW("nsfw"),
        SEX("sex"),
        ABSTRACT_EXPRESSIONISM("abstract-expressionism"),
        ACADEMIA("academia"),
        ACTION_FIGURE("action-figure"),
        ADORABLE_3D_CHARACTER("adorable-3d-character"),
        ADORABLE_KAWAII("adorable-kawaii"),
        ART_DECO("art-deco"),
        ART_NOUVEAU("art-nouveau"),
        ASTRAL_AURA("astral-aura"),
        AVANT_GARDE("avant-garde"),
        BAROQUE("baroque"),
        BAUHAUS_STYLE_POSTER("bauhaus-style-poster"),
        BLUEPRINT_SCHEMATIC_DRAWING("blueprint-schematic-drawing"),
        CARICATURE("caricature"),
        CEL_SHADED_ART("cel-shaded-art"),
        CHARACTER_DESIGN_SHEET("character-design-sheet"),
        CLASSICISM_ART("classicism-art"),
        COLOR_FIELD_PAINTING("color-field-painting"),
        COLORED_PENCIL_ART("colored-pencil-art"),
        CONCEPTUAL_ART("conceptual-art"),
        CONSTRUCTIVISM("constructivism"),
        CUBISM("cubism"),
        DADAISM("dadaism"),
        DARK_FANTASY("dark-fantasy"),
        DARK_MOODY_ATMOSPHERE("dark-moody-atmosphere"),
        DMT_ART("dmt-art"),
        DOODLE_ART("doodle-art"),
        DOUBLE_EXPOSURE("double-exposure"),
        DRIPPING_PAINT_SPLATTER("dripping-paint-splatter"),
        EXPRESSIONISM("expressionism"),
        FADED_POLAROID_PHOTO("faded-polaroid-photo"),
        FAUVISM("fauvism"),
        FLAT_2D("flat-2d"),
        FORTNITE_STYLE("fortnite-style"),
        FUTURISM("futurism"),
        GLITCHCORE("glitchcore"),
        GLO_FI("glo-fi"),
        GOOGIE_STYLE("googie-style"),
        GRAFFITI_ART("graffiti-art"),
        HARLEM_RENAISSANCE_ART("harlem-renaissance-art"),
        HIGH_FASHION("high-fashion"),
        IDYLLIC("idyllic"),
        IMPRESSIONISM("impressionism"),
        INFOGRAPHIC_DRAWING("infographic-drawing"),
        INK_DRIPPING_DRAWING("ink-dripping-drawing"),
        JAPANESE_INK_DRAWING("japanese-ink-drawing"),
        KNOLLING_PHOTOGRAPHY("knolling-photography"),
        LIGHT_CHEERY_ATMOSPHERE("light-cheery-atmosphere"),
        LOGO_DESIGN("logo-design"),
        LUXURIOUS_ELEGANCE("luxurious-elegance"),
        MACRO_PHOTOGRAPHY("macro-photography"),
        MANDOLA_ART("mandola-art"),
        MARKER_DRAWING("marker-drawing"),
        MEDIEVALISM("medievalism"),
        MINIMALISM("minimalism"),
        NEO_BAROQUE("neo-baroque"),
        NEO_BYZANTINE("neo-byzantine"),
        NEO_FUTURISM("neo-futurism"),
        NEO_IMPRESSIONISM("neo-impressionism"),
        NEO_ROCOCO("neo-rococo"),
        NEOCLASSICISM("neoclassicism"),
        OP_ART("op-art"),
        ORNATE_AND_INTRICATE("ornate-and-intricate"),
        PENCIL_SKETCH_DRAWING("pencil-sketch-drawing"),
        POP_ART_2("pop-art-2"),
        ROCOCO("rococo"),
        SILHOUETTE_ART("silhouette-art"),
        SIMPLE_VECTOR_ART("simple-vector-art"),
        SKETCHUP("sketchup"),
        STEAMPUNK_2("steampunk-2"),
        SURREALISM("surrealism"),
        SUPREMATISM("suprematism"),
        TERRAGEN("terragen"),
        TRANQUIL_RELAXING_ATMOSPHERE("tranquil-relaxing-atmosphere"),
        STICKER_DESIGNS("sticker-designs"),
        VIBRANT_RIM_LIGHT("vibrant-rim-light"),
        VOLUMETRIC_LIGHTING("volumetric-lighting"),
        WATERCOLOR("watercolor"),
        WHIMSICAL_AND_PLAYFUL("whimsical-and-playful"),
        SHARP("sharp"),
        MASTERPIECE("masterpiece"),
        PHOTOGRAPH("photograph"),
        NEGATIVE("negative"),
        CINEMATIC("cinematic"),
        ADS_ADVERTISING("ads-advertising"),
        ADS_AUTOMOTIVE("ads-automotive"),
        ADS_CORPORATE("ads-corporate"),
        ADS_FASHION_EDITORIAL("ads-fashion-editorial"),
        ADS_FOOD_PHOTOGRAPHY("ads-food-photography"),
        ADS_GOURMET_FOOD_PHOTOGRAPHY("ads-gourmet-food-photography"),
        ADS_LUXURY("ads-luxury"),
        ADS_REAL_ESTATE("ads-real-estate"),
        ADS_RETAIL("ads-retail"),
        ABSTRACT("abstract"),
        CONSTRUCTIVIST("constructivist"),
        CUBIST("cubist"),
        EXPRESSIONIST("expressionist"),
        GRAFFITI("graffiti"),
        HYPER_REALISM("hyperrealism"),
        IMPRESSIONIST("impressionist"),
        POINTILLISM("pointillism"),
        POP_ART("pop-art"),
        PSYCHEDELIC("psychedelic"),
        RENAISSANCE("renaissance"),
        STEAMPUNK("steampunk"),
        SURREALIST("surrealist"),
        TYPOGRAPHY("typography"),
        FUTURISTIC_BIOMECHANICAL("futuristic-biomechanical"),
        FUTURISTIC_BIOMECHANICAL_CYBERPUNK("futuristic-biomechanical-cyberpunk"),
        FUTURISTIC_CYBERNETIC("futuristic-cybernetic"),
        FUTURISTIC_CYBERNETIC_ROBOT("futuristic-cybernetic-robot"),
        FUTURISTIC_CYBERPUNK_CITYSCAPE("futuristic-cyberpunk-cityscape"),
        FUTURISTIC_FUTURISTIC("futuristic-futuristic"),
        FUTURISTIC_RETRO_CYBERPUNK("futuristic-retro-cyberpunk"),
        FUTURISTIC_RETRO("futuristic-retro"),
        FUTURISTIC_SCI_FI("futuristic-sci-fi"),
        FUTURISTIC_VAPORWAVE("futuristic-vaporwave"),
        GAME_BUBBLE("game-bubble"),
        GAME_CYBERPUNK("game-cyberpunk"),
        GAME_FIGHTING("game-fighting"),
        GAME_GTA("game-gta"),
        GAME_MARIO("game-mario"),
        GAME_MINECRAFT("game-minecraft"),
        GAME_POKEMON("game-pokemon"),
        GAME_RETRO_ARCADE("game-retro-arcade"),
        GAME_RETRO("game-retro"),
        GAME_RPG_FANTASY("game-rpg-fantasy"),
        GAME_STRATEGY("game-strategy"),
        GAME_STREETFIGHTER("game-streetfighter"),
        GAME_ZELDA("game-zelda"),
        MISC_ARCHITECTURAL("misc-architectural"),
        MISC_DISCO("misc-disco"),
        MISC_DREAMSCAPE("misc-dreamscape"),
        MISC_DYSTOPIAN("misc-dystopian"),
        MISC_FAIRY_TALE("misc-fairy-tale"),
        MISC_GOTHIC("misc-gothic"),
        MISC_GRUNGE("misc-grunge"),
        MISC_HORROR("misc-horror"),
        MISC_KAWAII("misc-kawaii"),
        MISC_LOVECRAFTIAN("misc-lovecraftian"),
        MISC_MACABRE("misc-macabre"),
        MISC_MANGA("misc-manga"),
        MISC_METROPOLIS("misc-metropolis"),
        MISC_MINIMALIST("misc-minimalist"),
        MISC_MONOCHROME("misc-monochrome"),
        MISC_NAUTICAL("misc-nautical"),
        MISC_SPACE("misc-space"),
        MISC_STAINED_GLASS("misc-stained-glass"),
        MISC_TECHWEAR_FASHION("misc-techwear-fashion"),
        MISC_TRIBAL("misc-tribal"),
        MISC_ZENTANGLE("misc-zentangle"),
        PAPERCRAFT_COLLAGE("papercraft-collage"),
        PAPERCRAFT_FLAT_PAPERCUT("papercraft-flat-papercut"),
        PAPERCRAFT_KIRIGAMI("papercraft-kirigami"),
        PAPERCRAFT_PAPER_MACHE("papercraft-paper-mache"),
        PAPERCRAFT_PAPER_QUILLING("papercraft-paper-quilling"),
        PAPERCRAFT_PAPERCUT_COLLAGE("papercraft-papercut-collage"),
        PAPERCRAFT_PAPERCUT_SHADOW_BOX("papercraft-papercut-shadow-box"),
        PAPERCRAFT_STACKED_PAPERCUT("papercraft-stacked-papercut"),
        PAPERCRAFT_THICK_LAYERED_PAPERCUT("papercraft-thick-layered-papercut"),
        PHOTO_ALIEN("photo-alien"),
        PHOTO_FILM_NOIR("photo-film-noir"),
        PHOTO_GLAMOUR("photo-glamour"),
        PHOTO_HDR("photo-hdr"),
        PHOTO_IPHONE_PHOTOGRAPHIC("photo-iphone-photographic"),
        PHOTO_LONG_EXPOSURE("photo-long-exposure"),
        PHOTO_NEON_NOIR("photo-neon-noir"),
        PHOTO_SILHOUETTE("photo-silhouette"),
        PHOTO_TILT_SHIFT("photo-tilt-shift"),
        THREE_D_MODEL("3d-model"),
        ANALOG_FILM("analog-film"),
        ANIME("anime"),
        COMIC_BOOK("comic-book"),
        CRAFT_CLAY("craft-clay"),
        DIGITAL_ART("digital-art"),
        FANTASY_ART("fantasy-art"),
        ISOMETRIC("isometric"),
        LINE_ART("line-art"),
        LOW_POLY("lowpoly"),
        NEON_PUNK("neonpunk"),
        ORIGAMI("origami"),
        PHOTOGRAPHIC("photographic"),
        PIXEL_ART("pixel-art"),
        TEXTURE("texture");

        companion object {
            private val map = values().associateBy(EnhanceType::value)
            fun fromString(value: String): EnhanceType? = map[value]

            fun fromOrdinal(value: Int): EnhanceType? = EnhanceType.values().getOrNull(value)
        }
    }

    enum class ModelId(val value: String) {
        FLUX("flux"),
        SDXLCESHI("sdxlceshi"),
        AE_SDXL_V1("ae-sdxl-v1"),
        CRYSTAL_CLEAR_XLV1("crystal-clear-xlv1"),
        PERFECT_WORLD_V61("perfect-world-v61"),
        FUWAFUWAMIX("fuwafuwamix"),
        TSHIRTDESIGNREDMOND("tshirtdesignredmond"),
        AE_T_PAGEPAL("ae-t-pagepal");

        companion object {
            fun fromOrdinal(value: Int): ModelId? = ModelId.values().getOrNull(value)
        }
    }

    data class Config (
        val modelType: ModelType,
        val negativePrompt: String? = null,
        val enhanceType: EnhanceType? = null,
        val modelId: ModelId? = null
    )

    enum class ModelType {
        COMMUNITY_API,
        REALTIME_API;

        companion object {
            fun fromOrdinal(value: Int): ModelType? = values().getOrNull(value)
        }
    }

    @Serializable
    data class ImageGenerationRequest(
        val key: String,
        val prompt: String,
        val negative_prompt: String? = null,
        val enhance_prompt: Boolean = false,
        val enhance_style: String? = null,
        val model_id: String? = null
    )

    @Serializable
    data class ImageGenerationResponse(
        val status: String? = null,
        val generationTime: Double? = null,
        val id: Long? = null,
        val output: List<String> = emptyList(),
        val proxy_links: List<String> = emptyList(),
        val meta: Meta? = null
    )

    @Serializable
    data class Meta(
        val base64: String,
        val enhance_prompt: String,
        val enhance_style: String?,
        val file_prefix: String,
        val guidance_scale: Int,
        val height: Int,
        val instant_response: String,
        val n_samples: Int,
        val negative_prompt: String,
        val opacity: Double,
        val outdir: String,
        val padding_down: Int,
        val padding_right: Int,
        val pag_scale: Double,
        val prompt: String,
        val rescale: String,
        val safety_checker: String,
        val safety_checker_type: String,
        val scale_down: Int,
        val seed: Long,
        val temp: String,
        val watermark: String,
        val width: Int
    )

    private val client = createOkHttpClientWithTimeouts()
    private val json = Json { ignoreUnknownKeys = true }
    private val log = logger()

    fun generateImage(prompt: String, config: Config, callback: (String?, Exception?) -> Unit) {
        log.info("MolesLabImageClient.generateImage, prompt: \"$prompt\", config: \"$config\"")
        val requestBody = ImageGenerationRequest(
            key = apiKey,
            prompt = prompt,
            negative_prompt = config.negativePrompt,
            enhance_prompt = config.enhanceType != null,
            enhance_style = config.enhanceType?.value,
            model_id = config.modelId?.value,
        )

        val requestBodyJson = json.encodeToString(requestBody)

        val url = when (config.modelType) {
            ModelType.REALTIME_API -> REALTIME_API_URL
            ModelType.COMMUNITY_API -> COMMUNITY_API_URL
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .post(requestBodyJson.toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null, e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        callback(null, IOException("Unexpected code $response"))
                    } else {
                        val responseBody = response.body?.string()
                        try {
                            val imageResponse = json.decodeFromString<ImageGenerationResponse>(responseBody ?: "")
                            val imageUrl = imageResponse.output.firstOrNull()
                            callback(imageUrl, null)
                        } catch (e: Exception) {
                            callback(null, e)
                        }
                    }
                }
            }
        })
    }
}