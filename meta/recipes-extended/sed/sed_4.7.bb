SUMMARY = "Stream EDitor (text filtering utility)"
HOMEPAGE = "http://www.gnu.org/software/sed/"
LICENSE = "GPLv3+"
LIC_FILES_CHKSUM = "file://COPYING;md5=c678957b0c8e964aa6c70fd77641a71e \
                    file://sed/sed.h;beginline=1;endline=15;md5=e869c874e5472ba89f63f381b0e93475"
SECTION = "console/utils"

SRC_URI = "${GNU_MIRROR}/sed/sed-${PV}.tar.xz \
           file://run-ptest \
"

SRC_URI[md5sum] = "777ddfd9d71dd06711fe91f0925e1573"
SRC_URI[sha256sum] = "2885768cd0a29ff8d58a6280a270ff161f6a3deb5690b2be6c49f46d4c67bd6a"

inherit autotools texinfo update-alternatives gettext ptest
RDEPENDS_${PN}-ptest += "make gawk perl perl-module-filehandle perl-module-file-compare perl-module-file-find perl-module-file-temp perl-module-file-stat"
RRECOMMENDS_${PN}-ptest_append_libc-glibc = " locale-base-ru-ru locale-base-en-us locale-base-el-gr.iso-8859-7"

EXTRA_OECONF = "--disable-acl \
               "

do_install () {
	autotools_do_install
	install -d ${D}${base_bindir}
	if [ ! ${D}${bindir} -ef ${D}${base_bindir} ]; then
	    mv ${D}${bindir}/sed ${D}${base_bindir}/sed
	    rmdir ${D}${bindir}/
	fi
}

ALTERNATIVE_${PN} = "sed"
ALTERNATIVE_LINK_NAME[sed] = "${base_bindir}/sed"
ALTERNATIVE_PRIORITY = "100"

do_compile_ptest() {
	oe_runmake testsuite/get-mb-cur-max testsuite/test-mbrtowc
}

do_install_ptest() {
        cp -rf ${S}/testsuite/ ${D}${PTEST_PATH}
        cp -rf ${B}/testsuite/* ${D}${PTEST_PATH}/testsuite/
        cp -rf ${S}/build-aux/ ${D}${PTEST_PATH}/
        cp ${B}/Makefile ${D}${PTEST_PATH}
        cp ${S}/init.cfg ${D}${PTEST_PATH}

        sed -e 's/^Makefile:/_Makefile:/' -e 's/^srcdir = \(.*\)/srcdir = ./' -e 's/bash/sh/' -i ${D}${PTEST_PATH}/Makefile
        for i in `grep -rl "sed/sed" ${D}${PTEST_PATH}`; do sed -e 's/..\/sed\/sed/sed/' -i $i; done

	sed -e 's,--sysroot=${STAGING_DIR_TARGET},,g' \
	    -e 's|${DEBUG_PREFIX_MAP}||g' \
	    -e 's:${HOSTTOOLS_DIR}/::g' \
	    -e 's:${RECIPE_SYSROOT_NATIVE}::g' \
	    -e 's:abs_top_builddir =.*:abs_top_builddir = ..:g' \
	    -e 's:abs_top_srcdir =.*:abs_top_srcdir = ..:g' \
	    -e 's:abs_srcdir =.*:abs_srcdir = ..:g' \
	    -e 's:top_srcdir =.*:top_srcdir = ..:g' \
	    -e 's:${BASE_WORKDIR}/${MULTIMACH_TARGET_SYS}::g' \
	    -i ${D}${PTEST_PATH}/Makefile
}

RPROVIDES_${PN} += "${@bb.utils.contains('DISTRO_FEATURES', 'usrmerge', '/bin/sed', '', d)}"
