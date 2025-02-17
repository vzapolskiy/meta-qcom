DESCRIPTION = "QCOM Firmware for DragonBoard 820c"

LICENSE = "Proprietary"
LIC_FILES_CHKSUM = "file://LICENSE;md5=4d087ee0965cb059f1b2f9429e166f64"

SRC_URI = "https://releases.linaro.org/96boards/dragonboard820c/qualcomm/firmware/linux-board-support-package-r${PV}.zip"
SRC_URI[md5sum] = "587138c5e677342db9a88d5c8747ec6c"
SRC_URI[sha256sum] = "6ee9c461b2b5dd2d3bd705bb5ea3f44b319ecb909b2772f305ce12439e089cd9"

FW_QCOM_NAME = "apq8096"

require recipes-bsp/firmware/firmware-qcom.inc

DEPENDS += "pil-squasher-native  qca-swiss-army-knife-native"
inherit python3native

S = "${WORKDIR}/linux-board-support-package-r${PV}"

do_compile() {
    # Build board-2.bin needed by WiFi
    ath10k-generate-pci-board-2_json.sh ./proprietary-linux/ board-2.json
    python3 "${STAGING_BINDIR_NATIVE}/ath10k-bdencoder" -c board-2.json -o board-2.bin
}

do_install() {
    install -d ${D}${nonarch_base_libdir}/firmware/
    install -d ${D}${FW_QCOM_PATH}/
    
    install -m 0444 ./proprietary-linux/adsp*.* ${D}${FW_QCOM_PATH}/
    pil-squasher ${D}${FW_QCOM_PATH}/adsp.mbn ./proprietary-linux/adsp.mdt

    install -m 0444 ./bootloaders-linux/adspso.bin ${D}${FW_QCOM_PATH}/

    install -d ${D}${nonarch_base_libdir}/firmware/ath10k/QCA6174/hw3.0/
    install -m 0444 ${S}/board-2.bin ${D}${nonarch_base_libdir}/firmware/ath10k/QCA6174/hw3.0/board-2.bin

    install -d ${D}${sysconfdir}/
    install -m 0644 LICENSE ${D}${sysconfdir}/QCOM-LINUX-BOARD-SUPPORT-LICENSE-${PN}

    # compat for Linux kernel <= 5.15
    install -d ${D}${FW_QCOM_BASE_PATH}/msm8996
    for file in ${D}${FW_QCOM_PATH}/*.mbn ${D}${FW_QCOM_PATH}/*.mdt ${D}${FW_QCOM_PATH}/*.b*
    do
        ln -s ../apq8096/$(basename $file) ${D}${FW_QCOM_BASE_PATH}/msm8996/
    done
}

inherit update-alternatives

ALTERNATIVE:${PN} = "qca6174-board2"
ALTERNATIVE_LINK_NAME[qca6174-board2] = "/lib/firmware/ath10k/QCA6174/hw3.0/board-2.bin"
ALTERNATIVE_PRIORITY = "100"

SPLIT_FIRMWARE_PACKAGES = " \
    ${PN}-dspso \
    linux-firmware-qcom-${FW_QCOM_NAME}-audio \
    linux-firmware-qcom-${FW_QCOM_NAME}-audio-split \
"
