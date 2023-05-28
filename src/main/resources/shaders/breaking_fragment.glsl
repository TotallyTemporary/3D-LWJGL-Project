#version 330 core
in vec3 pass_textureCoords;
out vec4 FragColor;

uniform sampler2DArray arrayTexture;
uniform int textureIndex;

void main()
{
    vec4 tex = texture(arrayTexture, pass_textureCoords);
    if (tex.a < 0.5) {
      discard;
    }

    vec4 breakageTexture = texture(arrayTexture, vec3(pass_textureCoords.xy, textureIndex));

    // for some unknown reason the texture doesn't have alpha.
    // so we discard values really close to the background colour 127/255 grey.
    const float halfBrightness = 127.0 / 255.0;
    if (abs(breakageTexture.r - halfBrightness) < 0.10) {
        discard;
    }

    FragColor = breakageTexture;
}