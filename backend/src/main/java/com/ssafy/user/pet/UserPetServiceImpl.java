package com.ssafy.user.pet;

import java.io.File;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ssafy.common.util.ImageFile;
import com.ssafy.common.util.ParameterCheck;
import com.ssafy.db.entity.Pet;
import com.ssafy.user.pet.request.PetRequest;

@Service("UserPetService")
public class UserPetServiceImpl implements UserPetService {
	
	@Autowired
	UserPetRepository petRepository;
	
	ParameterCheck parameterCheck = new ParameterCheck();
	
	ImageFile imageCheck = new ImageFile();
	
	@Override
	public boolean isValidPetInfo(PetRequest petRequest, boolean checkEmpty) {
		
		System.out.println("파일");
		// 파일 크기 및 확장자 유효성 검사
		MultipartFile petImage = petRequest.getPetImage();
		if (petImage != null)
			if (!parameterCheck.isValidImage(petImage, false)) return false;
		System.out.println("이름");
		// 반려동물 이름
		if (checkEmpty && petRequest.getPetName() == null) return false;
		System.out.println("대분류");
		// 반려동물 대분류
		String petType = petRequest.getPetType();
		if (checkEmpty && petType == null) return false;
		if (petType != null)
			if (!(petType.equals("개") || petType.equals("고양이") || petType.equals("토끼") || petType.equals("패럿") || petType.equals("기니피그") || petType.equals("햄스터"))) {
				return false;
			}
		System.out.println("생일");
		// 생일 확인
		String birth = petRequest.getPetBirth();
		if (birth != null)
			if (!Pattern.matches("^\\d{4}-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])$", birth)) {
				return false;
			}
		
		return true;
	}
	
	@Override
	public Pet addPet(String id, PetRequest petRequest) {
		
		boolean result = true;
		String imageName = null;
		
		// 파일 생성
		if (petRequest.getPetImage() != null) {
			
			MultipartFile imageFile = petRequest.getPetImage();
			
			// 파일 이름 생성
			imageName = imageCheck.makeFilename(imageFile.getOriginalFilename());
			
			// 이미지 크기 300px:300px로 조절해서 저장하기
			result = imageCheck.saveImage300(imageFile, imageName, "C:\\Users\\SSAFY");
		}
		
		if (result) {
			// Pet Entity 생성
			Pet pet = new Pet();
			pet.setUserId(id);
			if (imageName != null) pet.setPetImage(imageName);
			pet.setPetName(petRequest.getPetName());
			pet.setPetType(petRequest.getPetType());
			pet.setPetVariety(petRequest.getPetVariety());
			pet.setPetBirth(petRequest.getPetBirth());
			pet.setPetInfo(petRequest.getPetInfo());
			
			Pet saveResult = petRepository.save(pet);
			return saveResult;
		}
		
		return null;
	}
	
	@Override
	public boolean modifyPet(int no, PetRequest petRequest) {
		
		// Pet Entity 생성
		Pet pet = petRepository.findByNo(no);
		
		if (petRequest.getPetImage() != null) {
			
			// 기존 파일 가져오기
			String beforeFileName = pet.getPetImage();
			
			// 기존 파일 삭제하기
			File file = new File("C:\\Users\\SSAFY\\"+beforeFileName);
			file.delete();
			
			// 파일
			MultipartFile imageFile = petRequest.getPetImage();
			
			// 파일 이름 생성
			String imageName = imageCheck.makeFilename(imageFile.getOriginalFilename());
			
			// 이미지 크기 300px:300px로 조절해서 저장하기
			boolean result = imageCheck.saveImage300(imageFile, imageName, "C:\\Users\\SSAFY");
			
			if (result) pet.setPetImage(imageName);
		}
		
		if (petRequest.getPetName() != null && !petRequest.getPetName().equals(""))
			pet.setPetName(petRequest.getPetName());
		if (petRequest.getPetType() != null && !petRequest.getPetType().equals(""))
			pet.setPetType(petRequest.getPetType());
		if (petRequest.getPetVariety() != null && !petRequest.getPetVariety().equals(""))
			pet.setPetVariety(petRequest.getPetVariety());
		if (petRequest.getPetBirth() != null && !petRequest.getPetBirth().equals(""))
			pet.setPetBirth(petRequest.getPetBirth());
		if (petRequest.getPetInfo() != null && !petRequest.getPetInfo().equals(""))
			pet.setPetInfo(petRequest.getPetInfo());
		
		Pet result = petRepository.save(pet);
		if (result != null) return true;
		return false;
	}
	
}
